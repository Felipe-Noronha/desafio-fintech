<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Acesso ao Sistema</title>
    <style>
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }

        body {
            background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
            height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            color: #f8fafc;
        }

        .login-container {
            background: rgba(30, 41, 59, 0.7);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            padding: 40px;
            border-radius: 16px;
            width: 100%;
            max-width: 420px;
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.3), 0 10px 10px -5px rgba(0, 0, 0, 0.2);
            border: 1px solid rgba(255, 255, 255, 0.1);
        }

        .brand-header {
            text-align: center;
            margin-bottom: 32px;
        }

        .brand-header h1 {
            font-size: 28px;
            font-weight: 700;
            letter-spacing: -0.5px;
            background: linear-gradient(90deg, #38bdf8, #3b82f6);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            margin-bottom: 8px;
        }

        .brand-header p {
            color: #94a3b8;
            font-size: 14px;
        }

        .form-group {
            margin-bottom: 20px;
            position: relative;
        }

        label {
            display: block;
            font-size: 13px;
            font-weight: 600;
            color: #94a3b8;
            margin-bottom: 6px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        input {
            width: 100%;
            padding: 12px 16px;
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

        .btn-login {
            width: 100%;
            background: linear-gradient(90deg, #0284c7, #2563eb);
            color: #white;
            padding: 14px;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.2s ease;
            display: flex;
            justify-content: center;
            align-items: center;
            margin-top: 24px;
            color: white;
        }

        .btn-login:hover {
            opacity: 0.95;
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
        }

        .btn-login:disabled {
            background: #334155;
            color: #64748b;
            cursor: not-allowed;
            transform: none;
            box-shadow: none;
        }

        .alert-box {
            display: none;
            background: rgba(239, 68, 68, 0.15);
            border: 1px solid rgba(239, 68, 68, 0.3);
            color: #fca5a5;
            padding: 12px;
            border-radius: 8px;
            font-size: 14px;
            text-align: center;
            margin-bottom: 20px;
            font-weight: 500;
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

        .footer-note {
            text-align: center;
            margin-top: 24px;
            font-size: 12px;
            color: #64748b;
        }
    </style>
</head>
<body>

<div class="login-container">
    <div class="brand-header">
        <h1>Dunnas Fintech</h1>
    </div>

    <div id="alertBox" class="alert-box"></div>

    <form id="loginForm">
        <div class="form-group">
            <label for="email">E-mail</label>
            <input type="email" id="email" required autocomplete="username">
        </div>

        <div class="form-group">
            <label for="password">Senha</label>
            <input type="password" id="password" required autocomplete="current-password">
        </div>

        <button type="submit" id="btnLogin" class="btn-login">
            <span id="btnText">Acessar Conta</span>
            <div id="btnSpinner" class="spinner"></div>
        </button>
    </form>

    <div class="footer-note">
        &copy; 2026
    </div>
</div>

<script>
    document.getElementById('loginForm').addEventListener('submit', function(e) {
        e.preventDefault();

        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value;
        
        const btnLogin = document.getElementById('btnLogin');
        const btnText = document.getElementById('btnText');
        const btnSpinner = document.getElementById('btnSpinner');
        const alertBox = document.getElementById('alertBox');

        alertBox.style.display = "none";
        btnLogin.disabled = true;
        btnText.innerText = "Autenticando...";
        btnSpinner.style.display = "inline-block";

        fetch('/api/v1/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: email, password: password })
        })
        .then(async response => {
            const data = await response.json();
            
            if (response.ok && data.token) {
                localStorage.setItem('token', data.token);

                const date = new Date();
                date.setTime(date.getTime() + (24 * 60 * 60 * 1000));
                document.cookie = "AUTH_TOKEN=" + data.token + "; path=/; expires=" + date.toUTCString() + "; SameSite=Strict";

                window.location.href = '/dashboard';
            } else {
                showError(data.message || "Credenciais incorretas ou usuário inválido");
                resetState();
            }
        })
        .catch(err => {
            showError("Erro crítico de comunicação com o servidor financeiro");
            resetState();
        });

        function showError(msg) {
            alertBox.innerText = msg;
            alertBox.style.display = "block";
        }

        function resetState() {
            btnLogin.disabled = false;
            btnText.innerText = "Acessar Conta";
            btnSpinner.style.display = "none";
        }
    });
</script>

</body>
</html>