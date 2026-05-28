# Core de Pagamentos

## 1. Visão Geral da Arquitetura

Para atender aos pilares de alta confiabilidade, segurança e rastreabilidade transacional, a arquitetura foi dividida de maneira estratégica entre dois fluxos centrais de processamento:

### Fluxo Síncrono (Transferências Internas)

* **API RESTful:** As transferências entre contas internas ocorrem via chamadas HTTP síncronas para garantir o imediatismo da resposta operacional.
* **Isolamento Transacional:** Toda a operação financeira síncrona executa sob escopos controlados de `@Transactional` do Spring, garantindo propriedades ACID.

### Fluxo Assíncrono (Pix)

* **Arquitetura Orientada a Eventos:** O fluxo de Pix foi completamente desacoplado por meio do **Apache Kafka**.
* **Fluxo Operacional:** A API recebe a intenção de Pix, valida as regras superficiais e responde imediatamente com o status `202 Accepted`. O processamento real do débito e crédito é delegado ao consumidor do Kafka (`PixConsumer`), operando de forma assíncrona em background.

---

## 2. Justificativa das Principais Decisões Técnicas

Para assegurar o cumprimento dos itens classificados como inegociáveis (saldo negativo, duplicidade e inconsistência), foram adotadas as seguintes soluções arquiteturais:

### Prevenção de Saldo Negativo (Controle de Concorrência)

* **Bloqueio Pessimista de Escrita (`PESSIMISTIC_WRITE`):** Requisições simultâneas ou concorrentes sobre a mesma conta são controladas no banco de dados através da cláusula `SELECT FOR UPDATE` (gerada pela query nativa com a anotação `@Lock` no repositório). Isso impede de forma absoluta condições de corrida.
* **Prevenção Estatística de Deadlocks:** Visando mitigar o risco de travamento mútuo do banco de dados quando dois usuários efetuam transferências recíprocas ao mesmo tempo, foi implementada uma ordenação de travamento baseada no menor e maior ID numérico envolvido (`Math.min(id1, id2)` e `Math.max(id1, id2)`). O banco de dados enfileira os locks previsivelmente, eliminando a ocorrência de deadlocks.

### Prevenção de Duplicidade (Idempotência)

* **Mecanismo de Window-Lock:** O serviço de transferência gerencia um mapa em memória (`ConcurrentHashMap`). Antes de tocar no banco de dados, é computada uma chave baseada na assinatura da transação (`payerId_payeeId_amount`). Se uma requisição idêntica chegar de forma sobreposta, ela é imediatamente interceptada e rejeitada com código HTTP `400 Bad Request`.

### Criptografia de Senhas e Carga Inicial Segura

* **BCrypt Hashing:** A persistência de dados de usuários cumpre os padrões estritos de segurança utilizando hashing com **BCrypt** (`BCryptPasswordEncoder`).

### Justificativa do Motor de Mensageria: Apache Kafka

Foi escolhido o **Apache Kafka** devido à necessidade explícita de **rastreabilidade e persistência histórica**. O Kafka opera como um log distribuído imutável. Enquanto o RabbitMQ descarta as mensagens processadas, o Kafka retém os eventos de Pix, permitindo auditorias temporais e reprocessamento futuro de mensagens.

---

## 5. Modelo de Dados

O desenho da persistência foi estruturado e versionado utilizando **Flyway Migrations**. A propriedade `spring.jpa.hibernate.ddl-auto` está definida como `none`, delegando 100% do controle de esquema para os scripts de migração.

```text
[tb_user] 1 ------ N [tb_account] 1 ------ N [tb_transaction]

tb_user
- id (PK)
- name
- email (Unique)
- password (BCrypt)
- role (ADMIN/CUSTOMER)

tb_account
- id (PK)
- user_id (FK)
- balance

tb_transaction
- id (PK - UUID)
- payer_account_id (FK)
- payee_account_id (FK)
- amount
- created_at
- status (SUCCESS / FAILED|motivo)
```

---

## 6. Instruções para Rodar Localmente

A aplicação foi preparada para subir de forma autônoma sem qualquer dependência de intervenção ou configuração manual.

### Passos para Execução

1. Certifique-se de que o Docker esteja ativo no ambiente.
2. Na raiz do projeto, execute o comando:

```bash
docker compose up --build
```

O Docker Compose gerenciará a saúde dos serviços (`postgres-db`, `zookeeper`, `kafka`), aplicará as migrações estruturais do Flyway e executará a carga inicial de dados.

A aplicação estará disponível em:

```text
http://localhost:8080/login
```

### Credenciais Pré-Carregadas para Homologação

**Senha padrão:** `123`

| Perfil     | E-mail               | Saldo Inicial                             |
| ---------- | -------------------- | ----------------------------------------- |
| CUSTOMER 1 | `cliente@email.com`  | R$ 500,00                                 |
| CUSTOMER 2 | `cliente2@email.com` | R$ 100,00                                 |
| ADMIN      | `admin@email.com`    | Acesso livre para gerenciamento de contas |

### Executar os Testes Unitários Automatizados

Possui **19 testes automatizados** cobrindo controllers, regras de segurança e serviços.

Para executar os testes:

```bash
docker compose run --rm app-test
```

---

## 7. Observabilidade e Resiliência contra Serviços Instáveis

### Integração com o Autorizador Externo (`https://desafio.rivs.com.br`)

Considerando que o serviço externo simula instabilidades de rede e falhas de 30%, o `TransferService` isola a aplicação de colapsos através de:

#### Timeouts Explícitos

Configuração de:

* **Connection Timeout:** 2 segundos
* **Read Timeout:** 3 segundos

Utilizando `SimpleClientHttpRequestFactory`, impedindo o travamento de threads do Tomcat por indisponibilidade externa.

#### Rollback Seguro

Falhas de comunicação ou timeouts são capturados no escopo transacional. O Spring desfaz qualquer movimentação financeira de saldo no banco de dados e retorna uma mensagem clara de erro.

### Logs Estruturados e Distributed Tracking

#### JSON Layout

Os logs da aplicação utilizam a extensão `JsonLayout` combinada com `JacksonJsonFormatter` via Logback. Toda a saída segue estrutura JSON pronta para ingestão automática em plataformas de observabilidade.

#### Trace ID via MDC

Implementação de `SecurityFilter` gerando um identificador exclusivo (`X-Trace-Id`) por requisição. O ID é propagado via `MDC` em todas as mensagens geradas na mesma thread operacional.

### Health Check

O status de saúde da aplicação e dos brokers está exposto em:

```text
http://localhost:8080/actuator/health
```

---

## 8. Tabela de Endpoints da API

| Contexto       | Método   | Endpoint                          | Perfil Necessário      | Descrição                                 |
| -------------- | -------- | --------------------------------- | ---------------------- | ----------------------------------------- |
| Autenticação   | `POST`   | `/api/v1/auth/login`              | Pública                | Autentica o usuário e devolve o Token JWT |
| Contas         | `POST`   | `/api/v1/accounts`                | `ADMIN / CUSTOMER`     | Cria uma nova conta bancária              |
| Contas         | `GET`    | `/api/v1/accounts/{id}`           | `ADMIN / Proprietário` | Retorna os detalhes e saldo da conta      |
| Contas         | `GET`    | `/api/v1/accounts/{id}/statement` | `ADMIN / Proprietário` | Exibe o extrato paginado                  |
| Contas         | `DELETE` | `/api/v1/accounts/{id}`           | `ADMIN / Proprietário` | Encerra a conta (saldo zerado)            |
| Transferências | `POST`   | `/api/v1/transfers`               | Autenticado            | Executa transferência interna síncrona    |
| Pix            | `POST`   | `/api/v1/pix`                     | Autenticado            | Envia intenção de Pix assíncrona ao Kafka |

---

## 9. Itens Desejáveis Implementados

### Front-end Integrado via JSP

Conforme sugerido na seção de desejáveis, foi desenvolvida uma camada visual em JSP para facilitar os testes manuais de ponta a ponta na interface.

Os arquivos estão localizados em:

```text
src/main/webapp/WEB-INF/jsp/
```

### Métricas expostas via Prometheus
A aplicação foi integrada com o **Micrometer Prometheus Registry**. O endpoint nativo do Actuator foi exposto e configurado de forma pública para permitir que servidores Prometheus coletem dados de performance do sistema.
* **Endpoint de Métricas:** `http://localhost:8080/actuator/prometheus`

---

## 10. Limitações e Próximos Passos (Roadmap)

Devido ao escopo de tempo delimitado de 7 dias, os seguintes itens ficaram planejados para futuras sprints:

### Dead Letter Queue (DLQ)

Atualmente, Pix que falham por regra de negócio geram auditoria com status `FAILED` no banco e sofrem commit no Kafka. Em cenários de indisponibilidade sistêmica severa, as mensagens deveriam ser roteadas para um tópico dedicado como:

```text
pix-transactions-dlq
```

Permitindo reprocessamento controlado e isolamento de falhas críticas.

### Idempotência Distribuída com Redis

O mecanismo atual de prevenção de cliques duplos utiliza `ConcurrentHashMap` em memória. Para suportar escalabilidade horizontal, esse controle deve migrar para Redis com TTL configurado.

### Circuit Breaker e Tolerância a Falhas externas

A aplicação colheria excelentes resultados de resiliência com a integração do **Resilience4j** para gerenciar Circuit Breakers na comunicação com o autorizador externo.

* **Como funcionaria:** Caso o serviço parceiro ficasse indisponível por um período prolongado, o circuito abriria automaticamente, interceptando e rejeitando as requisições direto na nossa API. Isso pouparia o processamento interno do servidor, evitaria o esgotamento do pool de threads do Tomcat por timeout.

---

## 11. Fora de Escopo e Roadmap Futuro

As seguintes implementações foram mapeadas como fora do escopo inicial para esta entrega, sendo tratadas como limitações atuais e direcionadas para o roadmap futuro de evolução do projeto:

### Rastreamento Distribuído
* **Status Atual:** A aplicação implementa um rastreamento focado e isolado em nível de instância utilizando `X-Trace-Id` acoplado ao MDC (*Mapped Diagnostic Context*) do Logback, o que permite buscar o comportamento de uma requisição de ponta a ponta na thread do servidor através dos logs estruturados em JSON.
* **Abordagem de Escopo Real:** Para cenários de produção com alta escalabilidade, o ideal seria acoplar o **Spring Cloud Sleuth** (ou OpenTelemetry) integrado com coletores centrais como **Jaeger** ou **Zipkin**. Isso permitiria visualizar o gráfico de dependências e os gargalos de latência de ponta a ponta, principalmente na comunicação assíncrona que envolve o broker do Apache Kafka e as chamadas ao Autorizador Externo.

### Auditoria de Ações do Usuário
* **Status Atual:** As movimentações de saldo e o histórico básico de sucesso ou falha das transações de Pix e Transferências são persistidos diretamente na tabela `tb_transaction`, garantindo a consistência dos dados exigida pelo negócio.
* **Abordagem de Escopo Real:** Um sistema financeiro em produção exige uma trilha de auditoria imutável e separada, gravando logs de alteração baseados no padrão *Event Sourcing*. Em uma evolução do ecossistema, toda ação administrativa (como abertura e encerramento de contas por perfis `ADMIN`) ou tentativas de acessos negados seriam publicadas em um tópico exclusivo do Kafka (`fintech-audit-log`) para armazenamento e compliance, sem misturar com o banco de dados relacional operacional.

## 12. Como Testar os Endpoints

Para facilitar a validação e os testes dos endpoints da API, disponibilizei uma coleção pronta para uso. O arquivo encontra-se na raiz deste projeto:

* **Postman Collection:** `Desafio Dunnas - Gateway de Pagamentos.postman_collection.json`









