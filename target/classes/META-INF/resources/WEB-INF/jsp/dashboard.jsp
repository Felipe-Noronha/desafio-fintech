<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard</title>
    
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    
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
            padding: 40px 20px;
            color: #f8fafc;
        }

        .container {
            max-width: 950px;
            background: rgba(30, 41, 59, 0.7);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            padding: 35px;
            border-radius: 16px;
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.3), 0 10px 10px -5px rgba(0, 0, 0, 0.2);
            border: 1px solid rgba(255, 255, 255, 0.1);
            margin: 0 auto;
        }

        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-bottom: 1px solid #334155;
            padding-bottom: 20px;
        }

        .header h2 {
            font-size: 24px;
            font-weight: 600;
            background: linear-gradient(90deg, #38bdf8, #3b82f6);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }
        
        .header-actions {
            display: flex;
            gap: 10px;
            align-items: center;
        }

        .balance-card {
            background: #0f172a;
            border: 1px solid #334155;
            padding: 24px;
            border-radius: 12px;
            margin: 24px 0;
            box-shadow: inset 0 2px 4px 0 rgba(0, 0, 0, 0.2);
        }

        .balance-label {
            color: #94a3b8;
            font-size: 14px;
            font-weight: 500;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 6px;
        }

        .balance-amount {
            font-size: 36px;
            font-weight: 700;
            color: #10b981;
        }

        .section-title-container {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-top: 35px;
            margin-bottom: 15px;
        }

        .section-title-container h3 {
            font-size: 18px;
            font-weight: 600;
            color: #f1f5f9;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 10px;
            background: #0f172a;
            border-radius: 12px;
            overflow: hidden;
            border: 1px solid #334155;
        }

        th, td {
            padding: 14px 16px;
            text-align: left;
            font-size: 14px;
        }

        th {
            background-color: #1e293b;
            color: #94a3b8;
            font-weight: 600;
            text-transform: uppercase;
            font-size: 12px;
            letter-spacing: 0.5px;
            border-bottom: 1px solid #334155;
        }

        td {
            border-bottom: 1px solid #1e293b;
            color: #cbd5e1;
        }

        tr:last-child td {
            border-bottom: none;
        }

        code {
            background: #1e293b;
            padding: 4px 8px;
            border-radius: 6px;
            color: #f1f5f9;
            font-family: 'Courier New', Courier, monospace;
            font-size: 13px;
        }

        .btn {
            padding: 10px 18px;
            text-decoration: none;
            border-radius: 8px;
            font-weight: 600;
            font-size: 14px;
            display: inline-block;
            border: none;
            cursor: pointer;
            transition: all 0.2s ease;
        }

        .btn:hover {
            opacity: 0.95;
            transform: translateY(-1px);
        }

        .btn-payment {
            background: linear-gradient(90deg, #10b981, #059669);
            color: white;
        }

        .btn-manage {
            background: linear-gradient(90deg, #0284c7, #2563eb);
            color: white;
        }

        .btn-logout {
            background: #334155;
            color: #f1f5f9;
            border: 1px solid #475569;
        }

        .btn-logout:hover {
            background: #ef4444;
            color: white;
            border-color: #ef4444;
        }

        .btn-refresh {
            background-color: #1e293b;
            color: #f1f5f9;
            border: 1px solid #334155;
            padding: 8px 14px;
            border-radius: 6px;
            font-size: 13px;
            font-weight: 600;
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: 8px;
            transition: all 0.2s ease;
        }

        .btn-refresh:hover {
            background-color: #334155;
            border-color: #475569;
        }

        .btn-refresh:disabled {
            opacity: 0.5;
            cursor: not-allowed;
        }

        .pagination-container {
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 15px;
            margin-top: 20px;
        }

        .btn-nav {
            background: #1e293b;
            border: 1px solid #334155;
            color: #f8fafc;
            padding: 8px 14px;
            border-radius: 6px;
            font-weight: 600;
            cursor: pointer;
            font-size: 13px;
            transition: all 0.2s ease;
        }

        .btn-nav:hover:not(:disabled) {
            background: #334155;
            border-color: #475569;
        }

        .btn-nav:disabled {
            opacity: 0.4;
            cursor: not-allowed;
        }

        .page-info {
            font-size: 13px;
            color: #94a3b8;
            font-weight: 500;
        }

        .alert-error-box {
            display: none; 
            background: rgba(239, 68, 68, 0.15);
            border: 1px solid rgba(239, 68, 68, 0.3);
            color: #fca5a5;
            padding: 14px 18px;
            border-radius: 8px;
            font-size: 14px;
            margin-bottom: 24px;
            font-weight: 500;
            align-items: center;
            gap: 10px;
        }

        .spin {
            animation: rotation 0.8s linear infinite;
            display: inline-block;
        }

        @keyframes rotation {
            from { transform: rotate(0deg); }
            to { transform: rotate(360deg); }
        }
    </style>
</head>
<body>

<input type="hidden" id="currentAccountId" value="${account.id}">
<input type="hidden" id="statementLimit" value="${param.limit != null ? param.limit : 50}">

<div class="container">
    <div class="header">
        <h2>Olá, <c:out value="${user.name}"/>!</h2>
        
        <div class="header-actions">
            <a href="/accounts/manage" class="btn btn-manage">Gerenciar Contas</a>
            <a href="/transfer" class="btn btn-payment">Realizar Pagamento</a>
            <button onclick="logout()" class="btn btn-logout">Sair</button>
        </div>
    </div>

    <div id="asyncErrorBox" class="alert-error-box"></div>

    <div class="balance-card">
        <div class="balance-label">Saldo Disponível</div>
        <div class="balance-amount" id="balanceDisplay">R$ <c:out value="${account.balance}"/></div>
    </div>

    <div class="section-title-container">
        <h3>Extrato Recente de Transações</h3>
        <button id="refreshBtn" onclick="refreshDashboardData()" class="btn-refresh">
            <span id="refreshIcon" class="fa-solid fa-arrows-rotate"></span> Atualizar
        </button>
    </div>

    <table>
        <thead>
            <tr>
                <th>ID da Transação</th>
                <th>Destino</th>
                <th>Status</th>
                <th>Valor</th>
                <th>Data / Hora</th>
            </tr>
        </thead>
        <tbody id="transactionTableBody"></tbody>
    </table>

    <div class="pagination-container">
        <button id="btnPrev" class="btn-nav" onclick="changePage(-1)"><i class="fa-solid fa-chevron-left"></i> Anterior</button>
        <span id="pageIndicator" class="page-info">Página 1 de 1</span>
        <button id="btnNext" class="btn-nav" onclick="changePage(1)">Próxima <i class="fa-solid fa-chevron-right"></i></button>
    </div>
</div>

<script>
    let allTransactions = [];
    let currentPage = 1;
    const rowsPerPage = 5;

    function logout() {
        localStorage.removeItem('token');
        document.cookie = "AUTH_TOKEN=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC; SameSite=Strict";
        window.location.href = '/login';
    }

    function formatDate(rawDateString) {
        if (!rawDateString) return "";
        return new Date(rawDateString).toLocaleString('pt-BR');
    }

    document.addEventListener("DOMContentLoaded", function() {
        refreshDashboardData();
        setTimeout(refreshDashboardData, 1200);
    });

    function renderTablePage() {
        const tbody = document.getElementById('transactionTableBody');
        const accountId = document.getElementById('currentAccountId').value;
        const errorBox = document.getElementById('asyncErrorBox');
        tbody.innerHTML = "";

        if (allTransactions.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; color: #64748b; padding: 30px 0;">Nenhuma transação encontrada para esta conta</td></tr>';
            document.getElementById('pageIndicator').innerText = "Página 1 de 1";
            document.getElementById('btnPrev').disabled = true;
            document.getElementById('btnNext').disabled = true;
            return;
        }

        const ultimaTx = allTransactions[0];
        const isErroExibido = window.getComputedStyle(errorBox).display !== "none";
        
        if (ultimaTx.status && ultimaTx.status.startsWith("FAILED") && !isErroExibido) {
            const causa = ultimaTx.status.split('|')[1] || "Recusada";
            errorBox.innerHTML = '<i class="fa-solid fa-circle-exclamation"></i> <div><strong>Aviso do Sistema:</strong> Sua última tentativa de Pix de R$ ' + ultimaTx.amount.toFixed(2) + ' falhou.<br><small style="color: #fca5a5;">Motivo: ' + causa + '</small></div>';
            errorBox.style.display = "flex";
        } else if (ultimaTx.status === "SUCCESS" || !ultimaTx.status) {
            errorBox.style.display = "none";
        }

        const startIndex = (currentPage - 1) * rowsPerPage;
        const endIndex = startIndex + rowsPerPage;
        const paginatedItems = allTransactions.slice(startIndex, endIndex);
        const totalPages = Math.ceil(allTransactions.length / rowsPerPage);

        paginatedItems.forEach(tx => {
            const isPayer = tx.payerAccountId == accountId;
            let sign = isPayer ? "-" : "+";
            let valorColor = isPayer ? "#fca5a5" : "#34d399";
            let statusDisplay = '<span style="color: #34d399; font-weight: 600;">✓ Sucesso</span>';
            let destinoDisplay = "Conta " + tx.payeeAccountId;

            if (tx.status && tx.status.startsWith("FAILED")) {
                const partesStatus = tx.status.split('|');
                const causaReal = partesStatus.length > 1 ? partesStatus[1] : "Recusada";
                
                valorColor = "#94a3b8";
                statusDisplay = '<span style="color: #ef4444; font-weight: 700;">FALHOU: ' + causaReal + '</span>';
                destinoDisplay = "Conta " + tx.payeeAccountId + " (Rejeitada)";
            }

            const row = '<tr>' +
                '<td><code>' + tx.id + '</code></td>' +
                '<td>' + destinoDisplay + '</td>' +
                '<td>' + statusDisplay + '</td>' +
                '<td style="font-weight: 600; color: ' + valorColor + '">' + sign + ' R$ ' + tx.amount.toFixed(2) + '</td>' +
                '<td>' + formatDate(tx.createdAt) + '</td>' +
            '</tr>';
            
            tbody.innerHTML += row;
        });

        document.getElementById('pageIndicator').innerText = "Página " + currentPage + " de " + totalPages;
        document.getElementById('btnPrev').disabled = currentPage === 1;
        document.getElementById('btnNext').disabled = currentPage === totalPages || totalPages === 0;
    }

    function changePage(direction) {
        currentPage += direction;
        renderTablePage();
    }

    function refreshDashboardData() {
        const token = localStorage.getItem('token');
        const accountId = document.getElementById('currentAccountId').value;
        const limit = document.getElementById('statementLimit').value;
        
        if (!token || !accountId) return;

        const refreshBtn = document.getElementById('refreshBtn');
        const refreshIcon = document.getElementById('refreshIcon');

        refreshBtn.disabled = true;
        refreshIcon.classList.add('spin');

        fetch('/api/v1/accounts/' + accountId, {
            headers: { 'Authorization': 'Bearer ' + token }
        })
        .then(res => {
            if (!res.ok) throw new Error();
            return res.json();
        })
        .then(account => {
            document.getElementById('balanceDisplay').innerText = "R$ " + account.balance.toFixed(2);
        })
        .catch(err => console.error("Erro ao sincronizar saldo"));

        fetch('/api/v1/accounts/' + accountId + '/statement?limit=' + limit, {
            headers: { 'Authorization': 'Bearer ' + token }
        })
        .then(res => {
            if (!res.ok) throw new Error();
            return res.json();
        })
        .then(transactions => {
            allTransactions = transactions;
            renderTablePage();
        })
        .catch(err => console.error("Erro ao sincronizar extrato"))
        .finally(() => {
            setTimeout(() => {
                refreshBtn.disabled = false;
                refreshIcon.classList.remove('spin');
            }, 600);
        });
    }
</script>

</body>
</html>