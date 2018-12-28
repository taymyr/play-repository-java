[![Gitter](https://img.shields.io/badge/chat-gitter-purple.svg)](https://gitter.im/taymyr/taymyr)
[![Gitter_RU](https://img.shields.io/badge/chat-russian%20channel-purple.svg)](https://gitter.im/taymyr/taymyr_ru)
[![codebeat badge](https://codebeat.co/badges/cc5cf8e6-0145-480e-a412-fb525adfa56b)](https://codebeat.co/projects/github-com-taymyr-play-repository-java-develop)
[![Build Status](https://travis-ci.org/taymyr/play-repository-java.svg?branch=develop)](https://travis-ci.org/taymyr/play-repository-java)
[![Javadocs](https://www.javadoc.io/badge/org.taymyr.play/play-repository-api-java.svg?label=JavadocAPI)](https://www.javadoc.io/doc/org.taymyr.play/play-repository-api-java)
[![Javadocs](https://www.javadoc.io/badge/org.taymyr.play/play-repository-jpa-java.svg?label=JavadocJPA)](https://www.javadoc.io/doc/org.taymyr.play/play-repository-jpa-java)
[![codecov](https://codecov.io/gh/taymyr/play-repository-java/branch/develop/graph/badge.svg)](https://codecov.io/gh/taymyr/play-repository-java)
[![Maven](https://img.shields.io/maven-central/v/org.taymyr.play/play-repository-java.svg)](https://search.maven.org/search?q=a:play-repository-java%20AND%20g:org.taymyr.play)

# DDD Repository pattern for [Lagom](https://www.lagomframework.com)/[Play](https://playframework.com)

API of library contains only one interface [Repository](https://www.javadoc.io/doc/org.taymyr.play/play-repository-api-java) for DDD aggregate, inspired the book 
[Implementing Domain-Driven Design](https://www.amazon.com/Implementing-Domain-Driven-Design-Vaughn-Vernon/dp/0321834577) by Vaughn Vernon.

## Example

### JPA

Create the interface of repository for aggregate

```java
public interface AggregateRepository extends Repository<Aggregate, UUID> { }
```

and implement it

```java
public class AggregateRepositoryImpl extends JPARepository<Aggregate, UUID> implements AggregateRepository {

    @Inject
    public AggregateRepositoryImpl(@Nonnull JPAApi jpaApi, @Nonnull DatabaseExecutionContext executionContext) {
        super(jpaApi, executionContext, Aggregate.class);
    }

    @Override
    public UUID nextIdentity() {
        return UUID.randomUUID();
    }
}
```

## Contributors

Other persistence implementations (for _MongoDB_/_Cassandra_/_Redis_) are welcome.

## License

Copyright © Digital Economy League (https://www.digitalleague.ru/).

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this project except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

