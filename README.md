# Arquitetura Hexagonal com ArchUnit

API de reserva de salas de reunião construída em ports & adapters, com as fronteiras entre as camadas verificadas por testes de arquitetura.

O núcleo de negócio é Java puro: não conhece Spring, JPA nem HTTP. Em volta dele ficam quatro adaptadores — dois de entrada (REST e linha de comando) e dois de saída (PostgreSQL e memória) —, todos trocáveis sem alterar uma linha do domínio. As regras do ArchUnit transformam essa separação em algo que o build verifica, não em uma convenção que depende de disciplina.

## O domínio

Reserva de salas, com invariantes de verdade:

- Uma reserva não pode se sobrepor a outra confirmada na mesma sala.
- O número de participantes não pode exceder a capacidade.
- Não se reserva no passado.
- A duração fica entre 15 minutos e 8 horas.
- Não se cancela uma reserva que já começou.

Todas essas regras vivem no domínio, em classes que podem ser testadas sem subir nada.

## Estrutura

```
com.gkcontas.hexagonal
├── domain              núcleo: modelo, invariantes e exceções de negócio
├── application
│   ├── port.in         casos de uso (interfaces) e comandos
│   ├── port.out        repositórios e transação (interfaces)
│   └── service         implementação dos casos de uso, sem anotação de framework
├── adapter
│   ├── in.web          controllers REST, DTOs e tradução de erro para HTTP
│   ├── in.cli          runner de linha de comando
│   └── out.persistence
│       ├── jpa         entidades, Spring Data e mapeadores
│       └── memory      implementação em memória
└── config              fiação dos casos de uso como beans
```

As dependências apontam sempre para dentro. A camada de aplicação declara as interfaces de que precisa (`ReservationRepository`, `TransactionRunner`), e os adaptadores as implementam — é o adaptador que depende da aplicação, nunca o contrário.

## Tecnologias e bibliotecas

| | |
|---|---|
| Linguagem | Java 21 (records, text blocks, SequencedCollection) |
| Framework | Spring Boot 3.5 |
| Testes de arquitetura | ArchUnit 1.4 |
| Persistência | Spring Data JPA, PostgreSQL 16 |
| Migrations | Flyway |
| Validação | Bean Validation |
| Build | Gradle Kotlin DSL (wrapper `gradlew`) |
| Testes | JUnit 5, AssertJ, Testcontainers |
| Apoio | Lombok, somente nas entidades JPA |

## Pré-requisitos

- JDK 21 ou superior
- Docker, para o perfil com banco e para os testes de integração

## Como rodar

### Com PostgreSQL (perfil padrão)

```bash
docker compose up -d
```

```bash
./gradlew bootRun
```

A API fica em `http://localhost:8080`. O Flyway cria o schema e cadastra quatro salas na primeira subida.

### Sem banco nenhum (perfil `memory`)

```bash
./gradlew bootRun --args='--spring.profiles.active=memory'
```

Mesma API, mesmas regras, mesmas respostas — o adaptador de saída é um mapa. É a troca que a arquitetura promete, feita por configuração.

### Pela linha de comando (perfil `cli`)

```bash
./gradlew bootJar
```

```bash
java -jar build/libs/hexagonal-architecture-1.0.0.jar \
  --spring.profiles.active=jpa,cli --spring.main.web-application-type=none \
  rooms
```

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/rooms` | Lista as salas e suas capacidades |
| `POST` | `/reservations` | Cria uma reserva |
| `GET` | `/reservations/{id}` | Busca uma reserva |
| `DELETE` | `/reservations/{id}` | Cancela uma reserva |
| `GET` | `/reservations?roomId=&date=&zone=` | Agenda de uma sala em um dia |

Erros de negócio viram status HTTP no adaptador web: sala ocupada responde 409, capacidade ou horário inválido respondem 400, e identificador inexistente responde 404, sempre no formato RFC 7807.

## Exemplos de uso

```bash
curl -s localhost:8080/rooms
```

```bash
curl -s -X POST localhost:8080/reservations -H 'Content-Type: application/json' \
  -d '{"roomId":"AQUARIUM","organizer":"ana","start":"2026-11-01T10:00:00Z","end":"2026-11-01T11:00:00Z","attendees":4}'
```

Reservar o mesmo horário duas vezes:

```bash
curl -s -X POST localhost:8080/reservations -H 'Content-Type: application/json' \
  -d '{"roomId":"AQUARIUM","organizer":"bruno","start":"2026-11-01T10:00:00Z","end":"2026-11-01T11:00:00Z","attendees":2}'
```

```
{"title":"Room unavailable","status":409,
 "detail":"Room AQUARIUM is already booked during 2026-11-01 10:00 - 2026-11-01 11:00"}
```

Mais participantes do que a sala comporta:

```bash
curl -s -X POST localhost:8080/reservations -H 'Content-Type: application/json' \
  -d '{"roomId":"PHONE_BOOTH","organizer":"ana","start":"2027-01-10T10:00:00Z","end":"2027-01-10T11:00:00Z","attendees":9}'
```

Agenda do dia e cancelamento:

```bash
curl -s "localhost:8080/reservations?roomId=AQUARIUM&date=2026-11-01"
```

```bash
curl -s -X DELETE localhost:8080/reservations/<id>
```

## A linha de comando

O mesmo caso de uso, chamado por outro adaptador:

```bash
CLI="java -jar build/libs/hexagonal-architecture-1.0.0.jar \
  --spring.profiles.active=jpa,cli --spring.main.web-application-type=none --spring.main.banner-mode=off"
```

```bash
$CLI rooms
```

```bash
$CLI book --room=ATRIUM --organizer=carla --from=2026-11-16T09:00:00Z --to=2026-11-16T10:30:00Z --attendees=15
```

```bash
$CLI agenda --room=ATRIUM --date=2026-11-16
```

```bash
$CLI cancel --id=<id>
```

As falhas de negócio são as mesmas que a API devolve como 4xx, aqui impressas como texto:

```
error: Room ATRIUM is already booked during 2026-11-16 09:00 - 2026-11-16 10:30
```

## As regras de arquitetura

São 15 regras executadas como testes, em `HexagonalArchitectureTest`. As principais:

| Regra | O que ela impede |
|---|---|
| Regra de cebola | Dependência apontando para fora; um adaptador alcançando outro |
| Domínio sem framework | `@Component`, `@Entity` ou qualquer import de Spring/Jakarta no núcleo |
| Aplicação sem adaptador | Caso de uso chamando um controller ou um repositório Spring Data |
| Portas de saída só em adaptadores | Implementação de porta dentro da própria aplicação |
| Ausência de ciclos | Dependência circular entre pacotes |
| Nomenclatura por camada | Caso de uso sem o sufixo `UseCase`; serviço que não implementa porta de entrada |
| Entidades JPA restritas | `@Entity` fora do adaptador de persistência |
| Sem injeção por campo | `@Autowired` em atributo, que esconde a dependência do construtor |
| Console só na CLI | Escrita em `System.out` fora do adaptador de linha de comando |

Uma suíte de regras verde pode significar duas coisas: o código as respeita, ou as regras não casam com nada. Por isso `ArchitectureRulesBiteTest` aponta as mesmas regras para classes que as violam de propósito e exige que elas falhem.

## Testes

```bash
./gradlew test
```

84 testes, organizados pela mesma divisão da arquitetura:

| Camada | Classes | Testes | Característica |
|---|---|---|---|
| Domínio | `TimeSlotTest`, `RoomScheduleTest`, `ReservationTest` | 19 | Java puro, sem Spring e sem mock |
| Aplicação | `BookRoomServiceTest`, `CancelReservationServiceTest` | 11 | Casos de uso instanciados com `new`, usando os adaptadores em memória |
| Contrato de porta | `InMemoryReservationRepositoryTest`, `JpaReservationRepositoryTest` | 20 | As mesmas 10 asserções contra os dois adaptadores |
| Adaptador web | `ReservationControllerTest` | 8 | MockMvc no perfil `memory`, sem banco |
| Adaptador CLI | `ReservationCliRunnerTest` | 8 | Sem contexto Spring |
| Arquitetura | `HexagonalArchitectureTest`, `ArchitectureRulesBiteTest` | 18 | ArchUnit |

O teste de contrato é o que sustenta a afirmação de que os dois adaptadores de persistência são intercambiáveis: as mesmas asserções rodam contra o mapa e contra o PostgreSQL em container.

Nenhum teste de domínio, de aplicação, web ou CLI precisa de Docker — só os de contrato com JPA.

## Decisões de projeto

- **Transação como porta.** `TransactionRunner` é uma interface da aplicação, implementada com `TransactionTemplate` no adaptador JPA. Isso mantém o `@Transactional` fora do núcleo. A alternativa — anotar o serviço de aplicação — funciona e é o que a maioria dos times faz; custa a pureza que as regras de arquitetura garantem.
- **Três modelos.** Domínio, entidade JPA e DTO HTTP são classes diferentes, mapeadas explicitamente. É mais código, e é o que permite a entidade ter setters sem que o domínio perca suas invariantes.
- **Lombok só na persistência.** Um núcleo que se diz sem dependências não deveria precisar de um processador de anotações para compilar.
- **Clock injetado.** Metade das regras fala sobre passado e futuro; com o relógio como dependência, o teste decide onde fica o "agora".
