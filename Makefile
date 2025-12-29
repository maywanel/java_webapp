MVN ?= ./mvnw
SKIP_TESTS ?= false

WATCH ?= pom.xml src mvnw
RERUN_POLL_INTERVAL ?= 1
.PHONY: all clean install update  run

all: install

clean:
	@$(MVN) clean

install: clean
	@$(MVN) install -DskipTests=$(SKIP_TESTS)

update:
	@git pull --ff-only || true
	@$(MVN) -U dependency:resolve
	@$(MVN) install -DskipTests=$(SKIP_TESTS)

run:
	@mysql-start; \
	if ls target/*.jar >/dev/null 2>&1; then \
	  echo "Running jar from target/"; \
	  java -jar target/*.jar; \
	else \
	  echo "No jar in target/, attempting 'mvn spring-boot:run'"; \
	  $(MVN) spring-boot:run; \
	fi
