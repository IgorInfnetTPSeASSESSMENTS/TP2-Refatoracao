# Build Pipeline Refactoring Kata

## Sobre o projeto

Este projeto foi feito a partir do kata **BuildPipeline-Refactoring-Kata**, usado como base para praticar refatoração de código legado em Java.

A ideia do sistema é simular uma pipeline de build e deploy. O código original já funcionava, mas tinha vários problemas de organização e clareza, principalmente na classe `Pipeline`, que concentrava quase toda a lógica da aplicação em um único método.

O objetivo deste trabalho foi entender o funcionamento do sistema original, proteger o comportamento com testes e depois refatorar o código para deixá-lo mais legível, mais coeso e mais fácil de evoluir.

Também foi adicionada a nova regra pedida pelo kata: depois dos testes, a pipeline deve fazer deploy em **staging**, executar **smoke tests** e só então fazer deploy em **production**. Se não existirem smoke tests, a pipeline deve falhar e enviar a mensagem:

`Pipeline failed - no smoke tests`

---

## Como era o projeto original

Na versão original, a pipeline funcionava assim:

1. verificava se o projeto tinha testes;
2. se tivesse, executava os testes;
3. se os testes passassem, fazia o deploy;
4. no final, enviava ou não um e-mail resumo, dependendo da configuração.

O sistema era pequeno, mas o método principal da classe `Pipeline` tinha muita responsabilidade ao mesmo tempo. Ele decidia o fluxo, fazia logs, controlava o deploy e ainda definia a mensagem de e-mail.

Isso deixava o código difícil de ler e pior ainda para modificar.

---

## Problemas encontrados

Os principais problemas percebidos no código original foram:

- método `run()` muito grande para a responsabilidade dele;
- muitos `if/else` aninhados;
- uso de variáveis booleanas intermediárias que deixavam a lógica confusa;
- mistura entre regra de negócio, log e envio de e-mail;
- dificuldade para adicionar a nova etapa de staging e smoke tests sem piorar ainda mais o código.

Mesmo sendo um projeto pequeno, já dava para perceber problemas comuns de sistema legado: funciona, mas é chato de manter.

---

## Melhorias realizadas

### Refatoração da classe `Pipeline`

A principal refatoração foi feita na classe `Pipeline`.

Em vez de deixar toda a lógica concentrada no método `run()`, o fluxo foi quebrado em etapas menores e mais claras. Isso deixou a leitura bem mais simples e facilitou entender o que acontece em cada fase da pipeline.

A classe continuou sendo a orquestradora principal do sistema, mas com menos responsabilidade acumulada em um bloco só.

---

### Criação de `PipelineResult`

Foi criada a classe `PipelineResult` para representar o resultado final da pipeline.

Antes, o código dependia muito de flags booleanas como `testsPassed` e `deploySuccessful`. Isso funcionava, mas deixava o estado da execução espalhado pelo método.

Com `PipelineResult`, o resultado ficou encapsulado em um único objeto, o que melhorou bastante a clareza do código.

---

### Criação de `PipelineStatus`

Também foi criado o enum `PipelineStatus`, com os possíveis resultados finais da execução, como por exemplo:

- `SUCCESS`
- `TESTS_FAILED`
- `STAGING_DEPLOY_FAILED`
- `NO_SMOKE_TESTS`
- `SMOKE_TESTS_FAILED`
- `PRODUCTION_DEPLOY_FAILED`

Isso deixou o código mais expressivo e evitou depender só de combinações de booleanos ou mensagens soltas.

---

### Novo fluxo da pipeline

Depois da refatoração, a pipeline passou a seguir a regra nova do kata:

1. executar os testes;
2. fazer deploy em staging;
3. executar smoke tests;
4. se tudo passar, fazer deploy em production;
5. enviar e-mail com a mensagem adequada.

Se não houver smoke tests, a pipeline falha.

Também foram adicionadas mensagens de log e e-mails mais específicos, informando melhor em qual etapa aconteceu a falha.

---

## Testes

Antes de alterar o comportamento, foi importante olhar os testes já existentes e entender o que eles protegiam.

Como a regra da pipeline mudou, os testes antigos deixaram de refletir o comportamento esperado. Por isso, eles foram atualizados para cobrir os novos cenários, como:

- sucesso completo;
- falha nos testes;
- falha no deploy em staging;
- ausência de smoke tests;
- falha nos smoke tests;
- falha no deploy em production;
- execução com e sem envio de e-mail.

O teste de aprovação (`PipelineApprovalTest`) também precisou ser atualizado, já que a saída esperada mudou depois da nova lógica.

---

## Estrutura final

### Código principal

```text
src/main/java/
└── org/sammancoaching/
    ├── Pipeline.java
    ├── PipelineResult.java
    └── dependencies/
        ├── Config.java
        ├── DeploymentEnvironment.java
        ├── Emailer.java
        ├── Logger.java
        ├── PipelineStatus.java
        ├── Project.java
        └── TestStatus.java
```

### Testes

```text
src/test/java/
└── org/sammancoaching/
    ├── CapturingEmailer.java
    ├── CapturingLogger.java
    ├── DefaultConfig.java
    ├── PipelineApprovalTest.java
    └── PipelineTest.java
```
