# MoneyApp

Aplicação web para controle financeiro pessoal. Permite registrar contas bancárias, importar extratos, visualizar gastos e obter análises geradas por um modelo de linguagem rodando localmente.

## Tecnologias

**Backend**
- Java 21
- Spring Boot 4
- Spring Security com JWT
- Spring Data JPA
- PostgreSQL
- Ollama (LLM local) — compatível também com Groq via API

**Frontend**
- React 18 com TypeScript
- Vite
- Tailwind CSS
- Shadcn/ui
- Recharts

## Pré-requisitos

- Java 21
- Node.js 18+
- PostgreSQL rodando localmente
- Ollama instalado com o modelo desejado

```bash
# Instalar Ollama
curl -fsSL https://ollama.com/install.sh | sh

# Baixar modelo
ollama pull llama3
```

## Configuração

Crie um arquivo `.env` dentro de `backend/v1/` com as seguintes variáveis:

```env
DB_PORT=5432
DB_NAME=moneyapp
DB_USER=postgres
DB_PASSWORD=sua_senha
JWT_SECRET=seu_segredo_jwt
LLM_API_KEY=opcional_se_usar_groq
```

As propriedades de LLM ficam em `backend/v1/src/main/resources/application.properties`:

```properties
# Use "local" para Ollama ou "api" para Groq
llm.provider=local

llm.local.url=http://localhost:11434
llm.local.model=llama3
```

## Como rodar

**Backend**

```bash
cd backend/v1
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`.

**Frontend**

```bash
cd frontend
npm install
npm run dev
```

O frontend sobe em `http://localhost:5173`.

## Estrutura do projeto

```
MoneyApp/
├── backend/
│   └── v1/
│       └── src/main/java/com/moneyapp/v1/
│           ├── config/          # Configurações de segurança e Jackson
│           ├── controller/      # Endpoints REST
│           ├── dto/             # Objetos de transferência de dados
│           ├── enums/           # Enums de domínio
│           ├── exception/       # Exceções customizadas e handler global
│           ├── factory/         # Factories de criação de entidades
│           ├── middleware/       # Filtro JWT
│           ├── model/           # Entidades JPA
│           ├── repository/      # Repositórios Spring Data
│           ├── service/         # Lógica de negócio
│           │   └── llm/         # Abstração e implementações de LLM
│           └── specification/   # Filtros dinâmicos de transação
└── frontend/
    └── src/
        ├── components/          # Layout, sidebar e componentes UI
        ├── config/              # URL base da API
        ├── hooks/               # Hooks utilitários
        └── pages/               # Telas da aplicação
```

## Endpoints principais

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/users/create` | Cadastro de usuário |
| POST | `/users/login` | Login, retorna JWT |
| GET | `/dashboard` | Resumo financeiro do mês |
| GET/POST | `/account` | Gerenciar contas bancárias |
| GET/PUT/DELETE | `/transactions/{id}` | Gerenciar transações |
| POST | `/file/upload` | Upload de extratos |
| POST | `/extractor-file/{id}` | Extrair transações do documento via IA |
| GET | `/insights` | Análise financeira gerada por IA |
| POST | `/conversation` | Enviar mensagem ao assistente |

## Funcionalidades

- Cadastro e autenticação com controle de acesso por roles (USER, ADMIN, MODERATOR)
- Contas bancárias com tipo e moeda configuráveis
- Importação de extratos em PDF, CSV, XLSX e imagens com extração automática de transações via LLM
- Verificação de integridade de arquivo por hash SHA-256
- Filtros de transações por categoria, tipo e período
- Dashboard com gráfico de gastos por categoria, evolução mensal e lista de transações recentes
- Insights mensais comparando gastos atuais com a média dos últimos 3 meses
- Chat com assistente financeiro com histórico de conversa
