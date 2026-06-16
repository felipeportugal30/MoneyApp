# Relatório de Análise — MoneyApp

Análise do repositório MoneyApp, aplicação web de controle financeiro pessoal desenvolvida com Spring Boot 4 no backend e React/TypeScript no frontend.

---

## 1. Requisitos da Aplicação

### Implementados

| # | Requisito |
|---|-----------|
| 1 | Cadastro de usuário com validação de senha (mínimo 6 caracteres, letra maiúscula, número e caractere especial) |
| 2 | Autenticação via JWT com sessão stateless |
| 3 | Controle de acesso por roles: `USER`, `ADMIN`, `MODERATOR` |
| 4 | Gerenciamento de perfil: visualizar, atualizar nome/e-mail e excluir conta |
| 5 | Gerenciamento de contas bancárias: criar, listar, atualizar e excluir (soft delete) |
| 6 | Listagem de transações com filtros por categoria, tipo (EXPENSE/INCOME) e período |
| 7 | Edição de categoria de transação |
| 8 | Exclusão lógica de transação (`active = false`) |
| 9 | Upload de documentos: PDF, CSV, XLSX e imagens (JPG/PNG) |
| 10 | Verificação de integridade via hash SHA-256, bloqueando uploads duplicados ou corrompidos |
| 11 | Listagem e exclusão de documentos enviados |
| 12 | Extração automática de transações de documentos via LLM |
| 13 | Dashboard financeiro com gastos por categoria, evolução mensal e transações recentes |
| 14 | Insights financeiros gerados por IA comparando o mês atual com a média dos 3 meses anteriores |
| 15 | Chat com assistente financeiro com histórico de conversa e contexto financeiro do usuário |
| 16 | Interface do chat no frontend com renderização de markdown e histórico visual |
| 17 | Abstração de provedor LLM: suporte a modelo local (Ollama) e API externa (Groq), configurável por propriedade |

### Não Implementados

| # | Requisito |
|---|-----------|
| 1 | Refresh token — sessão expira em 1h sem renovação automática |
| 2 | Verificação de e-mail no cadastro |
| 3 | Recuperação de senha |
| 4 | Criação manual de transação sem upload de arquivo |
| 5 | Metas e limites de gastos por categoria |
| 6 | Exportação de dados em PDF ou CSV |
| 7 | Notificações e alertas de gastos |
| 8 | Suporte a múltiplas moedas no dashboard e insights |

---

## 2. Boas Práticas de POO

### Encapsulamento

Os campos das entidades são privados e acessados via Lombok (`@Getter`/`@Setter`). DTOs (`TransactionDto`, `UserResponseDto`, `AccountResponseDto`) separam os dados internos dos expostos pela API, impedindo que a entidade JPA seja serializada diretamente na resposta HTTP.

Ponto de melhoria: o uso de `@Getter @Setter` em todas as entidades expõe todos os campos sem distinção. `User.password` possui getter público, e entidades como `Transaction` têm setters para `active` e `deletedAt`, campos que deveriam ser controlados apenas pela lógica de negócio.

### Abstração

A interface `LLMProvider` define um contrato único para qualquer provedor de IA — o restante do sistema depende apenas desse contrato, sem conhecer a implementação concreta.

As factories (`UserFactory`, `AccountFactory`, `FileFactory`) abstraem a criação de objetos de domínio, isolando essa lógica dos services.

`FileExtractorService` abstrai o tipo do arquivo através do método `extractText(File)`, escondendo a lógica de detecção de formato.

### Herança

Não foi aplicada. Os modelos `User`, `Account`, `Transaction` e `File` repetem os mesmos campos de auditoria (`createdAt`, `updatedAt`, `deletedAt`). Uma superclasse abstrata eliminaria essa duplicação:

```java
@MappedSuperclass
public abstract class BaseEntity {
    @Column(nullable = false)
    private Date createdAt = new Date();
    private Date updatedAt;
    private Date deletedAt;
}
```

### Polimorfismo

`LLMProviderFactory` seleciona em tempo de execução entre `LocalLLMProvider` e `ApiLLMProvider`. O código cliente (`ConversationService`, `FinancialInsightService`) opera sobre a interface `LLMProvider` sem conhecer qual implementação está ativa.

---

## 3. Princípios SOLID

### S — Single Responsibility Principle

Controllers não contêm lógica de negócio — delegam integralmente para os services. `FinancialContextService` foi criado para isolar a responsabilidade de montar o contexto financeiro do usuário, que é compartilhada entre `FinancialInsightService` e `ConversationService`.

Ponto de melhoria: `FileExtractorService` acumula duas responsabilidades distintas — roteamento por tipo de arquivo e orquestração do processo de extração. Um `FileTypeRouter` separado deixaria cada classe com uma única razão para mudar.

```
Atual:  FileExtractorService  →  roteamento + orquestração
Ideal:  FileTypeRouter        →  roteamento por formato
        FileExtractorService  →  orquestração do processo
```

### O — Open/Closed Principle

Para adicionar um novo provedor de LLM basta criar uma classe que implemente `LLMProvider` e anotá-la com `@Service("nome")`. Nenhum código existente precisa ser alterado. O mesmo vale para filtros de transação: `TransactionSpecification` permite compor novos predicados sem modificar repositórios.

### L — Liskov Substitution Principle

`LocalLLMProvider` e `ApiLLMProvider` são completamente substituíveis. Qualquer componente que depende de `LLMProvider` funciona corretamente com qualquer uma das implementações, sem verificações de tipo ou comportamentos distintos.

### I — Interface Segregation Principle

`LLMProvider` expõe apenas o método `chat`, sem forçar implementações a suportar operações desnecessárias.

Ponto de melhoria: os services não possuem interfaces próprias. A ausência de `IUserService`, `ITransactionService` etc. dificulta testes unitários com mocks e torna o acoplamento maior do que o necessário.

### D — Dependency Inversion Principle

Controllers e services dependem de abstrações: interfaces do Spring Data JPA, `LLMProvider` e `LLMProviderFactory`.

Ponto identificado: `TransactionService` instanciava `new ObjectMapper()` diretamente, criando uma dependência concreta dentro de um módulo de alto nível e ignorando a configuração centralizada do `JacksonConfig`. A instância direta foi mantida por incompatibilidade de tipos entre as APIs Jackson 2.x e 3.x presentes no Spring Boot 4.

---

## 4. Clean Code

### Nomenclatura

Nomes de métodos descrevem intenção com clareza: `generateInsights`, `buildSystemPrompt`, `findAccountOwnedByUser`, `extractTransactions`.

Ponto de melhoria: parâmetros de método misturam snake_case e camelCase, contrariando a convenção Java:

```java
// Encontrado
public List<TransactionDto> process(UUID file_id, UUID accountId, User user)
public void deleteUser(UUID user_id, User userRequest)

// Correto
public List<TransactionDto> process(UUID fileId, UUID accountId, User user)
public void deleteUser(UUID userId, User userRequest)
```

### Funções

Métodos auxiliares privados com responsabilidade única estão presentes em toda a base: `toDto()` nos services, `formatChange()` em `FinancialContextService`, `buildPrompt()` em `ExtractionService`.

Ponto de melhoria: o prompt de extração em `ExtractionService.buildPrompt()` tem aproximadamente 50 linhas dentro do método. Externalizar esse conteúdo como constante de classe ou arquivo de recurso separaria configuração de lógica.

### DRY (Don't Repeat Yourself)

A extração de `FinancialContextService` eliminou duplicação de código que existia em `FinancialInsightService` e precisaria ser copiada para `ConversationService`.

Ponto de melhoria: o método `toDto()` é reimplementado em cada service. Uma camada de mappers dedicada centralizaria as conversões de entidade para DTO.

### Tratamento de Erros

Hierarquia de exceções de domínio personalizadas (`NotFoundException`, `InvalidRequestException`, `UnauthorizedException`) com `@RestControllerAdvice` centralizando todos os handlers.

Ponto de melhoria: o handler de `RuntimeException` retorna a mensagem bruta da exceção. Em produção, isso pode expor detalhes internos. Exceções não mapeadas deveriam retornar uma mensagem genérica padronizada.

### Magic Strings

A string `"portuguese"` é passada diretamente em `FileExtractorService`. Constantes ou um enum `Language` reduziriam erros de digitação silenciosos e facilitariam a manutenção.

```java
// Atual
extractionService.extractTransactions(rawText, "portuguese");

// Melhor
extractionService.extractTransactions(rawText, Language.PORTUGUESE);
```

---

## 5. Padrões de Projeto

### Factory Method

`UserFactory`, `AccountFactory` e `FileFactory` encapsulam a criação de objetos de domínio. `LLMProviderFactory` seleciona a implementação de LLM correta com base na propriedade `llm.provider`.

### Strategy

`LLMProvider` é a interface da estratégia. `LocalLLMProvider` e `ApiLLMProvider` são implementações concretas intercambiáveis em tempo de execução, sem impacto no código que as utiliza.

### Repository

Todos os repositórios estendem `JpaRepository`, abstraindo o acesso a dados e permitindo consultas sem SQL explícito.

### Specification

`TransactionSpecification` compõe predicados de consulta dinamicamente. Filtros por categoria, tipo e período são encadeados com `.and()` sem modificar o repositório.

### DTO (Data Transfer Object)

DTOs separados para entrada e saída em todos os recursos. A entidade JPA nunca é serializada diretamente na resposta HTTP.

### Padrões não aplicados — oportunidades

| Padrão | Onde aplicar |
|--------|-------------|
| Builder | Construção do system prompt em `ConversationService.buildSystemPrompt` |
| Template Method | Fluxo de `FileExtractorService.process`: extrair → chamar LLM → salvar |
| Observer | Notificações ao concluir extração ou atingir limite de gastos |

---

## 6. Padrões Arquiteturais

### Arquitetura em Camadas

```
Controller   →   recebe requisição HTTP, valida autenticação, delega
Service      →   regras de negócio, orquestração
Repository   →   acesso a dados via Spring Data JPA
Model        →   entidades de domínio
```

A separação é consistente. Controllers nunca acessam repositórios diretamente.

### REST

Verbos HTTP semânticos em todos os endpoints, status codes corretos: 201 para criação, 204 para deleção, 400/401/403/404 para erros.

Ponto de melhoria: nomenclatura inconsistente entre recursos:

| Endpoint atual | Problema | Sugestão |
|----------------|----------|----------|
| `/account` | singular | `/accounts` |
| `/extractor-file` | hífen, semântica ambígua | `/documents/extract` |
| `/conversation` | singular | `/conversations` |

### Segurança

Autenticação stateless com JWT. `JwtAuthFilter` intercepta todas as requisições, valida o token e popula o `SecurityContext`. `@PreAuthorize` protege rotas com verificação de role.

### Configuração Externalizada

Credenciais e propriedades sensíveis são carregadas de variáveis de ambiente via `.env`. O provedor de LLM, URL e modelo são configuráveis sem alterar código.

---

## Resumo

| Categoria | Situação |
|-----------|----------|
| Requisitos implementados | 17 de 25 identificados |
| Encapsulamento | Aplicado via DTOs; melhoria possível nas entidades com `BaseEntity` |
| Abstração e Polimorfismo | Bem aplicados via `LLMProvider` |
| Herança | Não utilizada; oportunidade clara com `BaseEntity` |
| SOLID | Bem aplicado no geral; ISP e DIP com pontos de melhoria |
| Clean Code | Nomenclatura e magic strings são os principais pontos de atenção |
| Design Patterns | Factory, Strategy, Repository, Specification e DTO identificados |
| Arquitetura | Camadas e REST consistentes; nomenclatura de endpoints a padronizar |
