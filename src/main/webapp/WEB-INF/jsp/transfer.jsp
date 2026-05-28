<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Operações de Pagamento</title>
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
            padding: 20px;
            overflow-y: auto;
        }

        .form-container {
            background: rgba(30, 41, 59, 0.7);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            padding: 30px 40px;
            border-radius: 16px;
            width: 100%;
            max-width: 480px;
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.3), 0 10px 10px -5px rgba(0, 0, 0, 0.2);
            border: 1px solid rgba(255, 255, 255, 0.1);
            margin: auto;
        }

        h2 {
            font-size: 24px;
            font-weight: 700;
            letter-spacing: -0.5px;
            background: linear-gradient(90deg, #38bdf8, #3b82f6);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            margin-bottom: 6px;
            text-align: center;
        }

        .subtitle {
            color: #94a3b8;
            font-size: 14px;
            margin-bottom: 20px;
            text-align: center;
        }

        .form-group {
            margin-bottom: 15px;
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

        input, select {
            width: 100%;
            padding: 10px 16px;
            background: #0f172a;
            border: 1px solid #334155;
            border-radius: 8px;
            color: #f8fafc;
            font-size: 15px;
            transition: all 0.2s ease;
        }

        input:focus, select:focus {
            outline: none;
            border-color: #38bdf8;
            box-shadow: 0 0 0 3px rgba(56, 189, 248, 0.15);
        }

        input:disabled {
            background: #1e293b;
            color: #64748b;
            border-color: #334155;
            cursor: not-allowed;
        }

        input::-webkit-outer-spin-button,
        input::-webkit-inner-spin-button {
            -webkit-appearance: none;
            margin: 0;
        }

        input[type=number] {
            -moz-appearance: textfield;
        }

        .btn-submit {
            width: 100%;
            background: linear-gradient(90deg, #10b981, #059669);
            color: white;
            padding: 12px;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            margin-top: 15px;
            margin-bottom: 10px;
            display: flex;
            justify-content: center;
            align-items: center;
            transition: all 0.2s ease;
        }

        .btn-submit:hover {
            opacity: 0.95;
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
        }

        .btn-submit:disabled {
            background: #334155;
            color: #64748b;
            cursor: not-allowed;
            transform: none;
            box-shadow: none;
        }
        
        .btn-cancel {
            display: block;
            width: 100%;
            background: #334155;
            color: #f1f5f9;
            border: 1px solid #475569;
            padding: 12px;
            border-radius: 8px;
            font-size: 15px;
            font-weight: 600;
            text-align: center;
            text-decoration: none;
            transition: all 0.2s ease;
        }

        .btn-cancel:hover {
            background: #475569;
        }

        .btn-cancel.disabled {
            background: #1e293b;
            color: #475569;
            border-color: #1e293b;
            pointer-events: none;
        }

        .spinner {
            display: none;
            width: 18px;
            height: 18px;
            border: 2px solid rgba(255, 255, 255, 0.3);
            border-radius: 50%;
            border-top-color: #f8fafc;
            animation: spin 0.6s linear infinite;
            margin-left: 10px;
        }

        @keyframes spin {
            to { transform: rotate(360deg); }
        }

        .status-message {
            display: none;
            padding: 10px;
            border-radius: 8px;
            margin-bottom: 15px;
            font-weight: 500;
            font-size: 14px;
            text-align: center;
        }
        
        .status-success {
            background: rgba(16, 185, 129, 0.15);
            border: 1px solid rgba(16, 185, 129, 0.3);
            color: #a7f3d0;
        }
        
        .status-error {
            background: rgba(239, 68, 68, 0.15);
            border: 1px solid rgba(239, 68, 68, 0.3);
            color: #fca5a5;
        }

        .badge-wrapper {
            text-align: center;
            margin-bottom: 5px;
        }

        .role-badge {
            display: inline-block;
            padding: 4px 10px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: 700;
            background: rgba(56, 189, 248, 0.15);
            color: #38bdf8;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
    </style>
</head>
<body>

<div class="form-container">
    <h2>Nova Operação Financeira</h2>
    <div class="badge-wrapper">
        <div class="role-badge" id="roleBadge">Verificando Perfil...</div>
    </div>
    <div class="subtitle" id="accountSubtitle">Sua conta: Carregando...</div>
    
    <div id="statusMessage" class="status-message"></div>

    <form id="paymentForm">
        <div class="form-group">
            <label for="payerId">Conta de Origem:</label>
            <input type="number" id="payerId" value="${userId}" required disabled>
        </div>

        <div class="form-group">
            <label for="type">Modalidade de Movimentação:</label>
            <select id="type" required>
                <option value="transfer">Transferência Interna</option>
                <option value="pix">Pix</option>
            </select>
        </div>

        <div class="form-group">
            <label for="payeeId">ID da Conta de Destino:</label>
            <input type="number" id="payeeId" required>
        </div>

        <div class="form-group">
            <label for="amount">Valor da Operação (R$):</label>
            <input type="number" id="amount" step="0.01" min="0.01" placeholder="0.00" required>
        </div>

        <button type="submit" id="btnSubmit" class="btn-submit">
            <span id="btnText">Confirmar Envio</span>
            <div id="btnSpinner" class="spinner"></div>
        </button>
    </form>

    <a href="/dashboard" id="btnCancel" class="btn-cancel">Voltar ao Painel</a>
</div>

<script>
    const token = localStorage.getItem('token');
    
    let userRole = "CUSTOMER";
    if (token) {
        try {
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
                return '%' + ('0' + c.charCodeAt(0).toString(16)).slice(-2);
            }).join(''));
            
            const parsedToken = JSON.parse(jsonPayload);
            userRole = parsedToken.role || "CUSTOMER";
        } catch (e) {
            console.error("Erro ao decodificar credenciais");
        }
    }

    document.addEventListener("DOMContentLoaded", function() {
        const payerInput = document.getElementById('payerId');
        const badge = document.getElementById('roleBadge');
        const subtitle = document.getElementById('accountSubtitle');

        badge.innerText = "Perfil: " + userRole;

        if (userRole === 'ADMIN') {
            payerInput.disabled = false;
            payerInput.placeholder = "ID da conta de origem";
            subtitle.innerText = "Modo de acesso irrestrito do Administrador";
            badge.style.background = "rgba(245, 158, 11, 0.15)";
            badge.style.color = "#fbbf24";
        } else {
            subtitle.innerText = "Sua conta ativa vinculada: Conta ID " + payerInput.value;
        }
    });

    document.getElementById('paymentForm').addEventListener('submit', function(e) {
        e.preventDefault();
        
        if (!token) {
            showStatus("Sessão expirada. Redirecionando para login...", false);
            setTimeout(() => { window.location.href = '/login'; }, 2000);
            return;
        }

        const btnSubmit = document.getElementById('btnSubmit');
        const btnCancel = document.getElementById('btnCancel');
        const btnText = document.getElementById('btnText');
        const btnSpinner = document.getElementById('btnSpinner');

        btnSubmit.disabled = true;
        btnCancel.classList.add('disabled');
        btnText.innerText = "Processando transação...";
        btnSpinner.style.display = "inline-block";
        
        const statusMessage = document.getElementById('statusMessage');
        statusMessage.style.display = "none";
        
        const type = document.getElementById('type').value;
        
        const originAccountId = parseInt(document.getElementById('payerId').value);

        const payload = {
            payerId: originAccountId,
            payeeId: parseInt(document.getElementById('payeeId').value),
            amount: parseFloat(document.getElementById('amount').value)
        };

        const endpoint = type === 'pix' ? '/api/v1/pix' : '/api/v1/transfers';

        fetch(endpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token 
            },
            body: JSON.stringify(payload)
        })
        .then(async response => {
            const rawText = await response.text();
            let displayMessage = rawText;

            try {
                if (rawText.startsWith("{")) {
                    const jsonParsed = JSON.parse(rawText);
                    if (jsonParsed.message) displayMessage = jsonParsed.message;
                }
            } catch(e) {}

            if (response.ok || response.status === 202) {
                showStatus("Operação realizada com sucesso! " + (type === 'pix' ? "Enviada para processamento" : ""), true);
                setTimeout(() => {
                    window.location.href = '/dashboard';
                }, 1800);
            } else {
                if (displayMessage.includes("Transferência recusada:")) {
                    showStatus(displayMessage, false);
                } else {
                    showStatus("Falha na transação: " + displayMessage, false);
                }
                resetFormState();
            }
        })
        .catch(err => {
            showStatus("Erro crítico: O servidor de pagamentos recusou a conexão", false);
            resetFormState();
        });

        if (userRole === 'ADMIN') {
            payerInput.disabled = false;
        }

        function showStatus(msg, isSuccess) {
            statusMessage.innerText = msg;
            statusMessage.className = "status-message " + (isSuccess ? "status-success" : "status-error");
            statusMessage.style.display = "block";
        }

        function resetFormState() {
            btnSubmit.disabled = false;
            btnCancel.classList.remove('disabled');
            btnText.innerText = "Confirmar Envio";
            btnSpinner.style.display = "none";
        }
    });
</script>

</body>
</html>