<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestão de Contas</title>
    <style>
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }

        body {
            background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            color: #f8fafc;
            padding: 40px 20px;
            overflow-y: auto;
        }

        .container {
            background: rgba(30, 41, 59, 0.7);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            padding: 35px 40px;
            border-radius: 16px;
            width: 100%;
            max-width: 620px;
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.3), 0 10px 10px -5px rgba(0, 0, 0, 0.2);
            border: 1px solid rgba(255, 255, 255, 0.1);
            margin: auto;
        }

        h2 {
            font-size: 26px;
            font-weight: 700;
            letter-spacing: -0.5px;
            background: linear-gradient(90deg, #38bdf8, #3b82f6);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            margin-bottom: 8px;
            text-align: center;
        }

        h3 {
            font-size: 18px;
            font-weight: 600;
            color: #f1f5f9;
            margin-bottom: 12px;
        }

        h4 {
            font-size: 15px;
            font-weight: 600;
            color: #fca5a5;
            margin-bottom: 8px;
        }

        p {
            color: #94a3b8;
            font-size: 14px;
            line-height: 1.5;
        }

        .form-group {
            margin-bottom: 16px;
        }

        label {
            display: block;
            font-size: 12px;
            font-weight: 600;
            color: #94a3b8;
            margin-bottom: 6px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        input {
            width: 100%;
            padding: 11px 16px;
            background: #0f172a;
            border: 1px solid #334155;
            border-radius: 8px;
            color: #f8fafc;
            font-size: 15px;
            transition: all 0.2s ease;
        }

        input:focus {
            outline: none;
            border-color: #38bdf8;
            box-shadow: 0 0 0 3px rgba(56, 189, 248, 0.15);
        }

        input::-webkit-outer-spin-button,
        input::-webkit-inner-spin-button {
            -webkit-appearance: none;
            margin: 0;
        }

        input[type=number] {
            -moz-appearance: textfield;
        }

        .btn {
            width: 100%;
            padding: 12px;
            border: none;
            border-radius: 8px;
            font-size: 15px;
            font-weight: 600;
            cursor: pointer;
            text-align: center;
            text-decoration: none;
            display: block;
            transition: all 0.2s ease;
            box-sizing: border-box;
            margin-bottom: 12px;
        }

        .btn:hover {
            opacity: 0.95;
            transform: translateY(-1px);
        }

        .btn-primary {
            background: linear-gradient(90deg, #0284c7, #2563eb);
            color: white;
        }
        .btn-primary:hover {
            box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
        }

        .btn-success {
            background: linear-gradient(90deg, #10b981, #059669);
            color: white;
        }
        .btn-success:hover {
            box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
        }

        .btn-danger {
            background: linear-gradient(90deg, #ef4444, #dc2626);
            color: white;
        }
        .btn-danger:hover {
            box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
        }

        .btn-secondary {
            background: #334155;
            color: #f1f5f9;
            border: 1px solid #475569;
            margin-top: 20px;
            margin-bottom: 0;
        }
        .btn-secondary:hover {
            background: #475569;
        }

        .status-box {
            display: none;
            padding: 12px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-weight: 500;
            font-size: 14px;
            text-align: center;
        }
        .success {
            background: rgba(16, 185, 129, 0.15);
            border: 1px solid rgba(16, 185, 129, 0.3);
            color: #a7f3d0;
        }
        .error {
            background: rgba(239, 68, 68, 0.15);
            border: 1px solid rgba(239, 68, 68, 0.3);
            color: #fca5a5;
        }

        .badge-wrapper {
            text-align: center;
            margin-bottom: 24px;
        }

        .role-badge {
            display: inline-block;
            padding: 4px 12px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: 700;
            background: rgba(56, 189, 248, 0.15);
            color: #38bdf8;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .section-divider {
            border: 0;
            border-top: 1px solid #334155;
            margin: 25px 0;
        }
        
        .danger-zone {
            border: 1px dashed rgba(239, 68, 68, 0.4);
            padding: 20px;
            border-radius: 12px;
            background: rgba(239, 68, 68, 0.05);
            margin-top: 20px;
        }

        .info-box {
            background: #0f172a;
            padding: 18px;
            border-radius: 12px;
            margin-bottom: 24px;
            border-left: 4px solid #10b981;
            box-shadow: inset 0 2px 4px 0 rgba(0, 0, 0, 0.2);
        }
    </style>
</head>
<body>

<div class="container">
    <h2>Gestão de Contas</h2>
    <div class="badge-wrapper">
        <div class="role-badge">Perfil Atual: ${userRole}</div>
    </div>

    <div id="statusBox" class="status-box"></div>

    <c:if test="${userRole == 'CUSTOMER'}">
        <div id="customerInfoArea" style="display: ${not empty account ? 'block' : 'none'};">
            <div class="info-box">
                <p style="color: #94a3b8; margin-bottom: 4px;">Sua conta principal ativa:</p>
                <p style="color: #f1f5f9; font-size: 15px; font-weight: 600; margin-bottom: 10px;">ID da Conta: <span id="displayAccountId">${account.id}</span></p>
                <p style="color: #94a3b8; margin-bottom: 4px;">Saldo:</p>
                <p style="color: #10b981; font-size: 22px; font-weight: 700;" id="displayAccountBalance">R$ ${account.balance}</p>
            </div>
        </div>

        <div id="customerCreationArea" style="margin-bottom: 10px;">
            <h3>Solicitar Nova Conta</h3>
            <p style="margin-bottom: 16px;">Clique abaixo para abrir uma nova conta associada ao seu perfil logado</p>
            <button onclick="createAccount(${userId}, 0.0)" class="btn btn-success">Solicitar Abertura de Conta</button>
        </div>
        
        <div id="customerDeleteArea" style="display: ${not empty account ? 'block' : 'none'};">
            <div class="danger-zone">
                <h4 style="color: #fca5a5;">Encerrar conta principal</h4>
                <p style="font-size: 13px; color: #94a3b8; margin-bottom: 16px;">Atenção: O encerramento definitivo só será processado se o saldo da conta estiver zerado</p>
                <button id="btnDeleteOwnAccount" onclick="deleteAccount('${account.id}', true)" class="btn btn-danger">Encerrar Minha Conta Definitivamente</button>
                
                <hr class="section-divider" style="margin: 15px 0;">
                
                <h4 style="color: #fca5a5;">Encerrar uma conta específica</h4>
                <div class="form-group">
                    <label for="customDeleteAccountId">ID da Conta:</label>
                    <input type="number" id="customDeleteAccountId" placeholder="Ex: 12">
                </div>
                <button onclick="executeCustomClientDeletion()" class="btn btn-danger" style="background: linear-gradient(90deg, #dc2626, #b91c1c); margin-bottom: 0;">Encerrar Conta Especificada</button>
            </div>
        </div>
    </c:if>

    <c:if test="${userRole == 'ADMIN'}">
        <div style="margin-bottom: 10px;">
            <h3>Abrir conta para usuário</h3>
            <div class="form-group">
                <label for="adminUserId">ID do usuário:</label>
                <input type="number" id="adminUserId" placeholder="Ex: 3">
            </div>
            <div class="form-group">
                <label for="adminBalance">Saldo inicial (R$):</label>
                <input type="number" id="adminBalance" value="0.00" step="0.01">
            </div>
            <button onclick="executeAdminCreation()" class="btn btn-primary">Criar Conta no Sistema</button>
        </div>

        <hr class="section-divider">

        <div>
            <h3>Encerrar Conta Bancária</h3>
            <div class="form-group">
                <label for="deleteAccountId">ID da Conta a ser Excluída:</label>
                <input type="number" id="deleteAccountId" placeholder="Ex: 5">
            </div>
            <p style="font-size: 13px; color: #fca5a5; margin-bottom: 16px;">Atenção: O encerramento definitivo só será processado se o saldo da conta estiver zerado</p>
            <button onclick="executeAdminDeletion()" class="btn btn-danger" style="margin-bottom: 0;">Encerrar Conta Definitivamente</button>
        </div>
    </c:if>

    <a href="/dashboard" class="btn btn-secondary">Voltar ao Painel</a>
</div>

<script>
    const token = localStorage.getItem('token');
    const userRole = "${userRole}";

    function createAccount(userId, initialBalance) {
        if (!token) return;

        fetch('/api/v1/accounts', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            body: JSON.stringify({ userId: userId, balance: initialBalance })
        })
        .then(async res => {
            const text = await res.text();
            if (res.ok || res.status === 201) {
                
                const infoArea = document.getElementById('customerInfoArea');
                const deleteArea = document.getElementById('customerDeleteArea');
                
                let createdId = "Sucesso";
                
                try {
                    const json = JSON.parse(text);
                    if (json.id) {
                        createdId = json.id;
                    } else if (json.accountId) {
                        createdId = json.accountId;
                    }
                } catch(e) {
                    const match = text.match(/\d+/);
                    if (match) createdId = match[0];
                }

                showMsg("Conta vinculada ID [" + createdId + "] aberta com sucesso!", true);

                if (userRole === 'CUSTOMER' && infoArea) {
                    document.getElementById('displayAccountId').innerText = createdId;
                    document.getElementById('displayAccountBalance').innerText = "R$ " + initialBalance.toFixed(2);
                    
                    const btnDelete = document.getElementById('btnDeleteOwnAccount');
                    if (btnDelete) btnDelete.setAttribute("onclick", "deleteAccount('" + createdId + "', true)");

                    infoArea.style.display = "block";
                    if (deleteArea) deleteArea.style.display = "block";
                }
            } else {
                showMsg("Falha na criação da conta: " + text, false);
            }
        })
        .catch(() => showMsg("Erro: O servidor recusou a conexão para abertura de conta", false));
    }

    function deleteAccount(accId, isOwnAccount) {
        if (!token || !accId || accId === "Sucesso" || accId === "Nova") {
            showMsg("ID de conta inválido ou inexistente", false);
            return;
        }

        fetch('/api/v1/accounts/' + accId, {
            method: 'DELETE',
            headers: { 'Authorization': 'Bearer ' + token }
        })
        .then(async res => {
            if (res.ok || res.status === 204) {
                showMsg("Conta ID " + accId + " foi encerrada com sucesso", true);
                
                if (isOwnAccount && userRole === 'CUSTOMER') {
                    const infoArea = document.getElementById('customerInfoArea');
                    const deleteArea = document.getElementById('customerDeleteArea');
                    if (infoArea) infoArea.style.display = "none";
                    if (deleteArea) deleteArea.style.display = "none";
                } else {
                    const inputCustom = document.getElementById('customDeleteAccountId');
                    const inputAdmin = document.getElementById('deleteAccountId');
                    if (inputCustom) inputCustom.value = "";
                    if (inputAdmin) inputAdmin.value = "";
                }
            } else {
                const text = await res.text();
                showMsg("Falha ao encerrar conta: " + text, false);
            }
        })
        .catch(() => showMsg("Erro de comunicação com o microsserviço de persistência", false));
    }

    function executeCustomClientDeletion() {
        const accId = document.getElementById('customDeleteAccountId').value.trim();
        if (!accId) {
            showMsg("Por favor, informe o ID da conta a ser encerrada", false);
            return;
        }
        deleteAccount(accId, false);
    }

    function executeAdminCreation() {
        const uId = parseInt(document.getElementById('adminUserId').value);
        const balance = parseFloat(document.getElementById('adminBalance').value || 0);
        if (!uId) {
            showMsg("Por favor, insira um ID de usuário válido", false);
            return;
        }
        createAccount(uId, balance);
    }

    function executeAdminDeletion() {
        const accId = document.getElementById('deleteAccountId').value.trim();
        if (!accId) {
            showMsg("Por favor, informe o ID de conta válido para exclusão", false);
            return;
        }
        deleteAccount(accId, false);
    }

    function showMsg(msg, isSuccess) {
        const box = document.getElementById('statusBox');
        box.innerText = msg;
        box.className = "status-box " + (isSuccess ? "success" : "error");
        box.style.display = "block";
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }
</script>

</body>
</html>