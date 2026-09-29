# Mottainai Backend

API REST do Mottainai para gestão de catálogo, fornecedores, empresas, lojas, estoque, alertas, promoções e fidelidade. É uma aplicação Spring Boot com PostgreSQL, Flyway e autenticação por JWT para a equipe e Firebase para o cliente no módulo de fidelidade.

## Requisitos

- Java 21
- PostgreSQL 15+
- Maven Wrapper (`bash ./mvnw`)
- Variáveis em `.env` no diretório raiz (não versionar esse arquivo):

```properties
DB_URL=jdbc:postgresql://localhost:5432/mottainai
DB_USER=seu_usuario
DB_PASSWORD=sua_senha
DB_APP_ROLE=role_da_aplicacao
DB_MIGRATION_USER=role_de_migracao
DB_MIGRATION_PASSWORD=senha_do_role_de_migracao
JWT_SECRET=<segredo-Base64-com-pelo-menos-32-bytes>
CORS_ALLOWED_ORIGINS=http://localhost:3000
FIREBASE_PROJECT_ID=seu-projeto-firebase
```

`FIREBASE_PROJECT_ID` é necessário apenas para autenticar chamadas em `/api/v1/client/**`. O JWT interno usa `JWT_SECRET` e o Firebase usa as chaves públicas oficiais para validar tokens de clientes.

## Banco de dados

A aplicação valida o mapeamento JPA (`spring.jpa.hibernate.ddl-auto=validate`).

O schema operacional de referência está no projeto `Mottainai-Banco-Operacional`. O banco precisa estar provisionado antes de iniciar a API. O Flyway fica desativado por padrão. Para desenvolvimento local, o perfil `local` executa apenas as migrations provisórias em `src/main/resources/db/migration-local`, partindo de um schema operacional já existente. As migrations V1 a V9 permanecem intactas.

Configure `DB_APP_ROLE` com um role de login existente, sem `SUPERUSER`, `BYPASSRLS`, propriedade das tabelas protegidas ou permissão de `CREATE` no schema `mottainai`. Configure `DB_MIGRATION_USER` como um role distinto, com permissão para aplicar as migrations e definir as funções de bootstrap. A senha de `DB_APP_ROLE` é informada em `DB_PASSWORD`. Execute o perfil local somente em banco de desenvolvimento isolado. V12 concede à aplicação os privilégios mínimos para os fluxos de autenticação e funcionários; os demais módulos ainda dependem dos grants operacionais próprios. V13 exige emails únicos sem diferenciar maiúsculas e valida cargo e loja nos tokens de recuperação e convite. A migration interrompe a instalação se já houver emails duplicados por essa regra. As estruturas e os grants provisórios de V10 a V13 precisam ser aprovados e incorporados ao banco operacional antes de usar esta versão da API fora desse ambiente.

## Executar

```bash
bash ./mvnw spring-boot:run
```

Para iniciar o perfil de desenvolvimento com migrations provisórias, defina `SPRING_PROFILES_ACTIVE=local` no ambiente antes de executar o comando acima.

Documentação interativa:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`
- Health check: `http://localhost:8080/actuator/health`

## Autenticação e autorização

### Equipe

1. Faça `POST /api/v1/auth/login` com CPF e senha.
2. Envie o access token retornado nas rotas de equipe:

```http
Authorization: Bearer <jwt>
```

`GET /api/v1/**` exige usuário autenticado. Operações de escrita exigem `ADMINISTRATOR` ou `MANAGER`; há restrições adicionais em empresas, planos, lojas e perfis de usuário.

### Cliente (fidelidade)

As rotas em `/api/v1/client/loyalty/**` exigem um Firebase ID token. O `sub` do token deve corresponder a `customer.external_auth_uid` de um cliente ativo.

```http
Authorization: Bearer <firebase-id-token>
```

## Endpoints

| Módulo | Rotas |
| --- | --- |
| Autenticação | `POST /api/v1/auth/login`, `POST /api/v1/auth/logout`, `POST /api/v1/auth/refresh`, `GET /api/v1/auth/profile`, `POST /api/v1/auth/password-recovery`, `PUT /api/v1/auth/password`, `GET /api/v1/auth/password-reset/validate`, `POST /api/v1/auth/password-reset` |
| Endereços | `POST, GET /api/v1/addresses`; `GET, PUT /api/v1/addresses/{id}` |
| Empresas | `POST, GET /api/v1/companies`; `GET, PUT, DELETE /api/v1/companies/{id}` |
| Planos | `POST, GET /api/v1/subscription-plans`; `GET, PUT, DELETE /api/v1/subscription-plans/{id}` |
| Lojas | `POST, GET /api/v1/stores`; `GET, PUT, DELETE /api/v1/stores/{id}` |
| Categorias | `POST, GET /api/v1/product-categories`; `GET, PUT, DELETE /api/v1/product-categories/{id}` |
| Produtos | `POST, GET /api/v1/products`; `GET, PUT, DELETE /api/v1/products/{id}`; `GET /api/v1/products/barcode/{barcode}` |
| Fornecedores | `POST, GET /api/v1/suppliers`; `GET, PUT, DELETE /api/v1/suppliers/{id}` |
| Produtos de fornecedor | `POST, GET /api/v1/supplier-products`; `GET, PUT, DELETE /api/v1/supplier-products/{id}` |
| Lotes | `POST, GET /api/v1/batches`; `GET /api/v1/batches/{id}` |
| Estoque | `GET, POST /api/v1/inventory`; `GET, PUT, DELETE /api/v1/inventory/{id}`; `GET /api/v1/inventory/barcode/{barcode}`; `GET /api/v1/inventory/expiring`; `GET, POST /api/v1/inventory/{id}/movements` |
| Alertas | `GET, POST /api/v1/alerts`; `GET /api/v1/alerts/{id}`; `POST /api/v1/alerts/{id}/resolve` |
| Sugestões | `GET, POST /api/v1/suggestions`; `GET /api/v1/suggestions/{id}`; `POST /api/v1/suggestions/{id}/approve`, `POST /api/v1/suggestions/{id}/reject` |
| Promoções | `GET, POST /api/v1/promotions`; `GET, PUT /api/v1/promotions/{id}`; `POST /api/v1/promotions/{id}/activate`, `POST /api/v1/promotions/{id}/deactivate` |
| Itens de promoção | `GET, POST /api/v1/promotions/{promotionId}/items`; `DELETE /api/v1/promotions/{promotionId}/items/{id}` |
| Funcionários | `POST /api/v1/employees`, `GET /api/v1/employees-store`, `GET /api/v1/employees-company`, `GET, PUT, DELETE /api/v1/employees/{id}`, `PUT /api/v1/employees/{id}/status`, `GET /api/v1/employees/{id}/audit-logs`, `GET /api/v1/employees/{id}/shifts`, `GET /api/v1/employees/{id}/cancel-request` |
| Fidelidade do cliente | `GET /api/v1/client/loyalty/balance`, `GET /api/v1/client/loyalty/transactions`, `POST /api/v1/client/loyalty/redeem` |

As listagens paginadas aceitam os parâmetros padrão do Spring Data, como `page`, `size` e `sort`. Consulte o Swagger para os DTOs de request/response, exemplos e códigos de erro de cada operação.

## Validação

```bash
bash ./mvnw -q -DskipTests compile
bash ./mvnw test
```

A compilação não requer acesso ao banco. A suíte que carrega o contexto integral requer `DB_URL`, `DB_USER` e `DB_PASSWORD` válidos e um schema compatível.
