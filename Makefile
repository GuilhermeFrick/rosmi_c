# ROSMI (C) -- build, testes unitarios e cobertura.
#
#   make              compila bin/rosmi_seq e bin/rosmi_par
#   make test         compila e roda a suite de testes unitarios
#   make coverage     roda a suite instrumentada e emite o relatorio gcov
#   make clean
#
# A versao sequencial NAO liga RosmiTask.c nem RosmiPosixTask.c: usa apenas os
# modulos funcionais, o que garante, e nao apenas promete, que nao ha
# concorrencia nela.
#
# Os dois programas ligam RosmiPng.c, que le a imagem PNG entregue com o
# enunciado. E o unico modulo que depende de biblioteca externa (libpng), por
# isso fica fora de CORE: o nucleo funcional continua compilando so com um
# compilador C.
#
# A versao paralela liga RosmiPosixTask.c, que sobrepoe por simbolo forte as
# duas funcoes fracas de criacao e juncao de thread. Trocar esse unico arquivo
# leva a aplicacao para outra plataforma.

# CC ja vem definido como "cc" pelas regras implicitas do make, entao ?= nao
# teria efeito; so sobrescrevemos quando o valor ainda e o padrao dele.
ifeq ($(origin CC),default)
CC      := gcc
endif
GCOV    ?= gcov
PYTHON  ?= python3

SRC     := src
TEST    := test
TOOLS   := tools
BUILD   := build
BIN     := bin
COV     := coverage

STD     := -std=c11
WARN    := -Wall -Wextra -Wpedantic -Wshadow -Wconversion -Wsign-conversion \
           -Wcast-qual -Wstrict-prototypes -Wmissing-prototypes
OPT     ?= -O2
CFLAGS  := $(STD) $(WARN) $(OPT) -I$(SRC)
PNGLIBS ?= -lpng

# Os testes usam o mesmo conjunto, menos -Wcast-qual: as macros EXPECT_STREQ
# do uTest recebem char* e nao const char*, entao comparar com um literal
# obriga a descartar a const na chamada. E limitacao da API do framework, nao
# do codigo de teste, e vale relaxar so aqui em vez de poluir cada chamada.
TESTWARN := $(filter-out -Wcast-qual,$(WARN))

# Modulos funcionais, sem nenhuma nocao de concorrencia.
CORE     := Rosmi RosmiImage RosmiUnionFind RosmiLabel RosmiMerge RosmiCli RosmiReport
# Camada de tarefas e o port que a torna funcional.
PAR      := RosmiTask RosmiPosixTask
# Entrada de imagem que depende de biblioteca externa.
IO       := RosmiPng
# Modulos cobrados pelo relatorio de cobertura.
COVERED  := $(CORE) $(PAR) $(IO)

CORE_SRC := $(addprefix $(SRC)/,$(addsuffix .c,$(CORE)))
PAR_SRC  := $(addprefix $(SRC)/,$(addsuffix .c,$(PAR)))
CORE_OBJ := $(addprefix $(BUILD)/,$(addsuffix .o,$(CORE)))
PAR_OBJ  := $(addprefix $(BUILD)/,$(addsuffix .o,$(PAR)))
IO_SRC   := $(addprefix $(SRC)/,$(addsuffix .c,$(IO)))
IO_OBJ   := $(addprefix $(BUILD)/,$(addsuffix .o,$(IO)))

# uTest.c vem do componente uTest e nao segue o conjunto estrito de avisos
# deste projeto, entao e compilado a parte com -w, para que o build so mostre
# achados que sao nossos.
UTEST_SRC := $(TEST)/uTest.c

TEST_SRC := $(TEST)/TestRosmiHost.c $(TEST)/TestRosmiAlloc.c $(TEST)/TestRosmiFixture.c \
            $(TEST)/TestRosmiUnionFind.c $(TEST)/TestRosmiImage.c $(TEST)/TestRosmiPng.c \
            $(TEST)/TestRosmiLabel.c $(TEST)/TestRosmiMerge.c \
            $(TEST)/TestRosmiTask.c $(TEST)/TestRosmiCli.c \
            $(TEST)/TestRosmiReport.c $(TEST)/TestRosmiMain.c

COV_APP_OBJ  := $(addprefix $(COV)/,$(addsuffix .o,$(COVERED)))
COV_TEST_OBJ := $(patsubst $(TEST)/%.c,$(COV)/%.o,$(TEST_SRC))

.PHONY: all clean test coverage dirs covdir

all: dirs $(BIN)/rosmi_seq $(BIN)/rosmi_par

dirs:
	@mkdir -p $(BUILD) $(BIN)

covdir:
	@mkdir -p $(COV)

$(BUILD)/%.o: $(SRC)/%.c | dirs
	$(CC) $(CFLAGS) -c $< -o $@

$(BIN)/rosmi_seq: $(CORE_OBJ) $(IO_OBJ) $(BUILD)/RosmiSeqMain.o
	$(CC) $(CFLAGS) $^ -o $@ $(PNGLIBS)

$(BIN)/rosmi_par: $(CORE_OBJ) $(PAR_OBJ) $(IO_OBJ) $(BUILD)/RosmiParMain.o
	$(CC) $(CFLAGS) $^ -o $@ -pthread $(PNGLIBS)

# ---- testes unitarios -----------------------------------------------------
# A suite liga TestRosmiAlloc.c, cujas definicoes fortes de RosmiMalloc e
# RosmiFree sobrepoem as fracas de Rosmi.c: e o que permite injetar falha de
# alocacao e contar blocos nao liberados. TestRosmiHost.c faz o mesmo pelo
# uTest, fornecendo TestWrite e TestGetTick.
test: dirs
	$(CC) $(STD) -w -O2 -I$(SRC) -I$(TEST) -c $(UTEST_SRC) -o $(BUILD)/uTest.o
	$(CC) $(STD) $(TESTWARN) $(OPT) -I$(SRC) -I$(TEST) $(CORE_SRC) $(PAR_SRC) $(IO_SRC) $(TEST_SRC) $(BUILD)/uTest.o \
		-o $(BIN)/rosmi_test -pthread $(PNGLIBS)
	@echo
	@$(BIN)/rosmi_test

# ---- cobertura ------------------------------------------------------------
# Cada fonte vira um objeto proprio em coverage/, para que os .gcno e os .gcda
# fiquem ao lado dele e o gcov os encontre. Compilar e ligar num passo so
# espalha esses arquivos pelo diretorio de trabalho.
#
# -O0 porque a otimizacao funde e reordena linhas, e o mapa do gcov deixa de
# corresponder ao fonte. So os modulos de src/ sao instrumentados: a cobertura
# do proprio codigo de teste nao diz nada sobre a aplicacao.
$(COV)/%.o: $(SRC)/%.c | covdir
	$(CC) $(STD) $(WARN) -O0 -g --coverage -I$(SRC) -I$(TEST) -c $< -o $@

$(COV)/%.o: $(TEST)/%.c | covdir
	$(CC) $(STD) $(TESTWARN) -O0 -g -I$(SRC) -I$(TEST) -c $< -o $@

$(COV)/uTest.o: $(UTEST_SRC) | covdir
	$(CC) $(STD) -w -O0 -g -I$(SRC) -I$(TEST) -c $< -o $@

coverage: covdir $(COV)/uTest.o $(COV_APP_OBJ) $(COV_TEST_OBJ)
	$(CC) --coverage $(COV)/uTest.o $(COV_APP_OBJ) $(COV_TEST_OBJ) \
		-o $(COV)/rosmi_test -pthread $(PNGLIBS)
	@./$(COV)/rosmi_test > $(COV)/rosmi_test.log
	@cd $(COV) && $(GCOV) -b -o . $(addprefix ../,$(CORE_SRC) $(PAR_SRC) $(IO_SRC)) > gcov.log 2>&1 || true
	@echo
	@echo "=== cobertura por modulo ==="
	@$(PYTHON) $(TOOLS)/gcov_summary.py $(COV)/gcov.log
	@echo
	@echo "relatorios .gcov detalhados em $(COV)/"

clean:
	rm -rf $(BUILD) $(BIN) $(COV) *.gcov *.gcda *.gcno test_image_tmp.pbm test_image_tmp*.png
