package com.fide;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import android.os.Environment;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

enum TipoToken {
    // tipos:
	BOOL,
    TEX,
	INT,
	FLUTU,
	DOBRO,
    NUMERO,
    // operadores mamaticos:
    ATRIBUICAO,
	ADICAO,
	SUBTRACAO,
	MULTIPLICACAO,
	DIVISAO,
	PORCENTAGEM,
	// condicionais:
	SE,
	SENAO,
	POR,
	ENQ,
	IGUAL_IGUAL,
	DIFERENTE,
	MAIOR,
	MENOR,
	MAIOR_IGUAL,
	MENOR_IGUAL,
    // variaveis:
    VARIAVEL,
	// metodos e classes
	CLASSE,
	INSTANCIA,
	NOVO,
    FUNCAO, 
    // nome:
    IDENTIFICADOR,
    // fecha e abre:
    PARENTESE_ESQ, 
    PARENTESE_DIR, 
    CHAVE_ESQ, 
    CHAVE_DIR,
	CONCHETE_ESQ,
	CONCHETE_DIR,
    // expressoes:
	PONTO,
    VIRGULA, 
    PONTO_VIRGULA,
	INTERROGACAO,
	RETORNE,
	ESTE,
	FALSO,
	VERDADE,
    FIM
	}

class Token {
    public final TipoToken tipo;
    public final String valor;

    public Token(TipoToken tipo, String valor) {
        this.tipo = tipo;
        this.valor = valor;
    }
}

class AnalisadorLexico {
    private final String codigo;
    private int posicao = 0;
    private final StringBuilder buffer = new StringBuilder(); // buffer

    public AnalisadorLexico(String codigo) {
        this.codigo = codigo;
        if(codigo.length()>100000) { // limite de tamanho do codigo
            System.err.println("codigo muito grande para processar");
        }
    }

    public List<Token> tokenizar() {
        List<Token> tokens = new ArrayList<>(codigo.length() / 10); // pre-alocação estimada

        while(posicao<codigo.length()) {
            char atual = codigo.charAt(posicao);

            if(Character.isWhitespace(atual)) {
                posicao++;
            } else if(Character.isLetter(atual)) {
                tokens.add(lerIdentificador());
            } else if(Character.isDigit(atual)) {
                tokens.add(lerNumero());
            } else if(tratarCaracteresSimples(tokens, atual)) {
            } else if(atual=='"' || atual=='\'') {
                tokens.add(lerString());
            } else {
                System.err.println("caractere invalido: '"+atual+"' na posicao "+posicao);
            }
        }

        tokens.add(new Token(TipoToken.FIM, ""));
        return tokens;
    }

    private boolean tratarCaracteresSimples(List<Token> tokens, char atual) {
        switch(atual) {
            case '(':
                tokens.add(new Token(TipoToken.PARENTESE_ESQ, "("));
                posicao++;
                return true;
            case ')':
                tokens.add(new Token(TipoToken.PARENTESE_DIR, ")"));
                posicao++;
                return true;
            case '{':
                tokens.add(new Token(TipoToken.CHAVE_ESQ, "{"));
                posicao++;
                return true;
            case '}':
                tokens.add(new Token(TipoToken.CHAVE_DIR, "}"));
                posicao++;
                return true;
			case '[':
                tokens.add(new Token(TipoToken.CONCHETE_ESQ, "["));
                posicao++;
                return true;
            case ']':
                tokens.add(new Token(TipoToken.CONCHETE_DIR, "]"));
                posicao++;
                return true;
            case ';':
                tokens.add(new Token(TipoToken.PONTO_VIRGULA, ";"));
                posicao++;
                return true;
            case ',':
                tokens.add(new Token(TipoToken.VIRGULA, ","));
                posicao++;
                return true;
			case '+':
                tokens.add(new Token(TipoToken.ADICAO, "+"));
                posicao++;
                return true;
			case '-':
                tokens.add(new Token(TipoToken.SUBTRACAO, "-"));
                posicao++;
                return true;
			case '*':
                tokens.add(new Token(TipoToken.MULTIPLICACAO, "*"));
                posicao++;
                return true;
			case '/':
                tokens.add(new Token(TipoToken.DIVISAO, "/"));
                posicao++;
                return true;
			case '%':
                tokens.add(new Token(TipoToken.PORCENTAGEM, "%"));
                posicao++;
                return true;
			case '!':
				if(posicao+1<codigo.length() && codigo.charAt(posicao+1)=='=') {
					tokens.add(new Token(TipoToken.DIFERENTE, "!="));
					posicao += 2;
				} else {
					System.err.println("operador invalido: '!'");
				}
				return true;
			case '=':
				if(posicao+1<codigo.length() && codigo.charAt(posicao+1)=='=') {
					tokens.add(new Token(TipoToken.IGUAL_IGUAL, "=="));
					posicao += 2;
				} else {
					tokens.add(new Token(TipoToken.ATRIBUICAO, "="));
					posicao++;
				}
				return true;
			case '>':
				if(posicao+1<codigo.length() && codigo.charAt(posicao + 1)=='=') {
					tokens.add(new Token(TipoToken.MAIOR_IGUAL, ">="));
					posicao += 2;
				} else {
					tokens.add(new Token(TipoToken.MAIOR, ">"));
					posicao++;
				}
				return true;
			case '<':
				if(posicao+1<codigo.length() && codigo.charAt(posicao + 1)=='=') {
					tokens.add(new Token(TipoToken.MENOR_IGUAL, "<="));
					posicao += 2;
				} else {
					tokens.add(new Token(TipoToken.MENOR, "<"));
					posicao++;
				}
				return true;
			case '.':
				tokens.add(new Token(TipoToken.PONTO, "."));
				posicao++;
				return true;
        }
        return false;
    }

    private Token lerIdentificador() {
        buffer.setLength(0);
        while(posicao < codigo.length() && Character.isLetterOrDigit(codigo.charAt(posicao))) {
            buffer.append(codigo.charAt(posicao));
            posicao++;
        }
        String valor = buffer.toString();
        return new Token(
			valor.equals("enq") ? TipoToken.ENQ :
			valor.equals("por") ? TipoToken.POR :
			valor.equals("classe") ? TipoToken.CLASSE :
			valor.equals("este") ? TipoToken.ESTE :
			valor.equals("novo") ? TipoToken.NOVO :
			valor.equals("func") ? TipoToken.FUNCAO :
			valor.equals("retorne") ? TipoToken.RETORNE :
			valor.equals("var") ? TipoToken.VARIAVEL :
			valor.equals("Bool") ? TipoToken.BOOL :
			valor.equals("falso") ? TipoToken.FALSO :
			valor.equals("verdade") ? TipoToken.VERDADE :
			valor.equals("Tex") ? TipoToken.TEX :
			valor.equals("Flutu") ? TipoToken.FLUTU :
			valor.equals("Dobro") ? TipoToken.DOBRO :
			valor.equals("Int") ? TipoToken.INT :
			valor.equals("se") ? TipoToken.SE :
			valor.equals("senao") ? TipoToken.SENAO :
			valor.equals("senão") ? TipoToken.SENAO :
			TipoToken.IDENTIFICADOR, valor
        );
    }

    private Token lerNumero() {
		buffer.setLength(0);
		boolean temPonto = false;

		while(posicao < codigo.length()) {
			char c = codigo.charAt(posicao);

			if(Character.isDigit(c)) {
				buffer.append(c);
				posicao++;
			} else if(c == '.' && !temPonto && (posicao + 1 < codigo.length() && Character.isDigit(codigo.charAt(posicao + 1)))) {
				temPonto = true;
				buffer.append(c);
				posicao++;
			} else {
				break;
			}
		}

		String valor = buffer.toString();
		return new Token(TipoToken.NUMERO, valor);
	}

    private Token lerString() {
		buffer.setLength(0);
		char delimitador = codigo.charAt(posicao++); // aspas simples ou dupla

		while(posicao < codigo.length()) {
			char c = codigo.charAt(posicao++);

			if(c == '\\') {
				if(posicao >= codigo.length()) break; // escape invalido no fim

				char proximo = codigo.charAt(posicao++);
				switch(proximo) {
					case 'n': buffer.append('\n'); break;
					case 't': buffer.append('\t'); break;
					case 'r': buffer.append('\r'); break;
					case '\'': buffer.append('\''); break;
					case '"': buffer.append('"'); break;
					case '\\': buffer.append('\\'); break;
					default: buffer.append(proximo); break; // caractere não reconhecido, mantem literal
				}
			} else if(c == delimitador) {
				return new Token(TipoToken.TEX, buffer.toString()); // fim da string
			} else {
				buffer.append(c);
			}
		}
		throw new RuntimeException("String não fechada");
	}
}

interface No {}

// operadores:
class NoAtribuicao implements No {
    String nome;
    TipoToken tipoDeclarado;
    No valor;

    public NoAtribuicao(String nome, TipoToken tipoDeclarado, No valor) {
        this.nome = nome;
        this.tipoDeclarado = tipoDeclarado;
        this.valor = valor;
    }
}

class NoValor implements No {
    String valor;
    TipoToken tipo;

    public NoValor(String valor, TipoToken tipo) {
        this.valor = valor;
        this.tipo = tipo;
    }
}

class NoAdicao implements No {
    No esquerda, direita;

    public NoAdicao(No esquerda, No direita) {
        this.esquerda = esquerda;
        this.direita = direita;
    }
}

class NoSubtracao implements No {
    No esquerda, direita;

    public NoSubtracao(No esquerda, No direita) {
        this.esquerda = esquerda;
        this.direita = direita;
    }
}

class NoMultiplicacao implements No {
    No esquerda, direita;

    public NoMultiplicacao(No esquerda, No direita) {
        this.esquerda = esquerda;
        this.direita = direita;
    }
}

class NoDivisao implements No {
    No esquerda, direita;

    public NoDivisao(No esquerda, No direita) {
        this.esquerda = esquerda;
        this.direita = direita;
    }
}

class NoPorcentagem implements No {
    No esquerda,  direita;

    public NoPorcentagem(No esquerda, No direita) {
        this.esquerda = esquerda;
        this.direita = direita;
    }
}

// variaveis
class NoVariavel implements No {
    String nome;

    public NoVariavel(String nome) {
        this.nome = nome;
    }
}

class NoArray implements No {
    List<No> itens;

    public NoArray(List<No> itens) {
        this.itens = itens;
    }
}

class NoAcessoArray implements No {
    No array;
    No indice;

    public NoAcessoArray(No array, No indice) {
        this.array = array;
        this.indice = indice;
    }
}

// metodos e classe:
class NoClasse implements No {
    String nome;
    List<No> membros;
    public NoClasse(String nome, List<No> membros) {
        this.nome = nome;
        this.membros = membros;
    }
}

// Adicionar nova classe para representar instâncias
class ObjetoInstancia {
    String nomeClasse;
    NoClasse definicao;
    Map<String, String> campos = new HashMap<>();

    ObjetoInstancia(String nomeClasse, NoClasse definicao) {
        this.nomeClasse = nomeClasse;
        this.definicao = definicao;
    }

    void definirCampo(String nome, String valor) {
        campos.put(nome, valor);
    }

    String obterCampo(String nome) {
        return campos.getOrDefault(nome, "vazio");
    }
}

class NoNovo implements No {
    String nome;
    public NoNovo(String nome) {
        this.nome = nome;
    }
}

class NoFuncao implements No {
    String nome;
    List<String> parametros;
    List<No> corpo;

    public NoFuncao(String nome, List<String> parametros, List<No> corpo) {
        this.nome = nome;
        this.parametros = parametros;
        this.corpo = corpo;
    }
}

class NoChamadaFuncao implements No {
    String nome;
    List<No> argumentos;

    public NoChamadaFuncao(String nome, List<No> argumentos) {
        this.nome = nome;
        this.argumentos = argumentos;
    }
}

class NoRetorne implements No {
    final No valor;

    public NoRetorne(No valor) {
        this.valor = valor;
    }
}

// condicionais:
class NoEnq implements No {
    No condicao;
    List<No> corpo;
    public NoEnq(No condicao, List<No> corpo) {
        this.condicao = condicao;
        this.corpo = corpo;
    }
}

class NoPor implements No {
    No inicializacao;
    No condicao;
    No incremento;
    List<No> corpo;
	
    public NoPor(No inicializacao, No condicao, No incremento, List<No> corpo) {
        this.inicializacao = inicializacao;
        this.condicao = condicao;
        this.incremento = incremento;
        this.corpo = corpo;
    }
}

class NoIgualIgual implements No {
    No esquerda, direita;

    public NoIgualIgual(No esq, No dir) {
		this.esquerda = esq;
		this.direita = dir;
	}
}

class NoDiferente implements No {
    No esquerda, direita;

    public NoDiferente(No esq, No dir) {
		this.esquerda = esq;
		this.direita = dir;
	}
}

class NoMaior implements No {
    No esquerda, direita;

    public NoMaior(No esq, No dir) {
		this.esquerda = esq;
		this.direita = dir;
	}
}

class NoMaiorIgual implements No {
    No esquerda, direita;

    public NoMaiorIgual(No esq, No dir) {
		this.esquerda = esq;
		this.direita = dir;
	}
}

class NoMenor implements No {
    No esquerda, direita;

    public NoMenor(No esq, No dir) {
		this.esquerda = esq;
		this.direita = dir;
	}
}

class NoMenorIgual implements No {
    No esquerda, direita;

    public NoMenorIgual(No esq, No dir) {
		this.esquerda = esq;
		this.direita = dir;
	}
}

class NoCondicional implements No {
    No condicao;
    List<No> blocoSe;
    List<No> blocoSenao;
	
    public NoCondicional(No cond, List<No> se, List<No> senao) {
        this.condicao = cond;
        this.blocoSe = se;
        this.blocoSenao = senao;
    }
}

class AnalisadorSintatico {
    private final List<Token> tokens;
    private int atual = 0;

    public AnalisadorSintatico(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<No> analisar() {
        List<No> nos = new ArrayList<>();
        while(!estaNoFim()) {
            nos.add(declaracao());
        }
        return nos;
    }

    private No declaracao() {
		if(verificar(TipoToken.FUNCAO)) return declaracaoFuncao();
		if(verificar(TipoToken.RETORNE)) return declaracaoRetorne();
		if(verificar(TipoToken.VARIAVEL)) return declaracaoVariavel();
		if(verificar(TipoToken.BOOL)) return declaracaoVariavel();
		if(verificar(TipoToken.TEX)) return declaracaoVariavel();
		if(verificar(TipoToken.INT)) return declaracaoVariavel();
		if(verificar(TipoToken.FLUTU)) return declaracaoVariavel();
		if(verificar(TipoToken.DOBRO)) return declaracaoVariavel();
		if(verificar(TipoToken.SE)) return declaracaoCondicional();
		if(verificar(TipoToken.ENQ)) return declaracaoEnq();
		if(verificar(TipoToken.POR)) return declaracaoPor();
		if(verificar(TipoToken.CLASSE)) return declaracaoClasse();

		//verificação para reatribuição
		if(olhar().tipo == TipoToken.IDENTIFICADOR && 
		   olharProximo(1).tipo == TipoToken.ATRIBUICAO) {
			Token nome = avancar();
			avancar(); // consome o '='
			No valor = lerComparacao();
			consumir(TipoToken.PONTO_VIRGULA, "Esperado ';' após atribuição");
			return new NoAtribuicao(nome.valor, null, valor); // Tipo null
		}

		return chamadaFuncao();
	}

	private Token olharProximo(int passos) {
		int posicaoFutura = atual + passos;
		return posicaoFutura < tokens.size() ? tokens.get(posicaoFutura) : tokens.get(tokens.size()-1);
	}
	
	private boolean tokenInicioExpressao(TipoToken tipo) {
		switch(tipo) {
			case NUMERO:
			case TEX:
			case IDENTIFICADOR:
			case PARENTESE_ESQ:
			case CONCHETE_ESQ:
			case SUBTRACAO: // pra numeros negativos
				return true;
			default:
				return false;
		}
	}
	
	private No lerArray() {
		consumir(TipoToken.CONCHETE_ESQ, "Esperado '['");
		List<No> itens = new ArrayList<>();

		if(!verificar(TipoToken.CONCHETE_DIR)) {
			do {
				itens.add(lerComparacao());
			} while(verificar(TipoToken.VIRGULA) && avancar() != null);
		}

		consumir(TipoToken.CONCHETE_DIR, "Esperado ']'");
		return new NoArray(itens);
	}

	private No lerComparacao() {
		if(fim() || verificar(TipoToken.PARENTESE_DIR) || verificar(TipoToken.CHAVE_DIR)) {
			return null;
		}

		if(!tokenInicioExpressao(olhar().tipo)) {
			return null;
		}

		No esquerda = lerAdicao();

		while(verificarOperadorComparacao()) {
			TipoToken operador = olhar().tipo;
			avancar();
			No direita = lerAdicao();

			switch(operador) {
				case IGUAL_IGUAL: 
					esquerda = new NoIgualIgual(esquerda, direita); break;
				case DIFERENTE:
					esquerda = new NoDiferente(esquerda, direita); break;
				case MAIOR: 
					esquerda = new NoMaior(esquerda, direita); break;
				case MENOR: 
					esquerda = new NoMenor(esquerda, direita); break;
				case MAIOR_IGUAL:
					esquerda = new NoMaiorIgual(esquerda, direita); break;
				case MENOR_IGUAL:
					esquerda = new NoMenorIgual(esquerda, direita); break;
			}
		}
		return esquerda;
	}

	private No lerAdicao() {
		No esquerda = lerMultiplicacao();

		while(verificar(TipoToken.ADICAO) || verificar(TipoToken.SUBTRACAO)) {
			TipoToken operador = olhar().tipo;
			avancar();
			No direita = lerMultiplicacao();

			if(operador == TipoToken.ADICAO) {
				esquerda = new NoAdicao(esquerda, direita);
			} else {
				esquerda = new NoSubtracao(esquerda, direita);
			}
		}
		return esquerda;
	}

	private No lerMultiplicacao() {
		No esquerda = lerPrimario();

		while(
			verificar(TipoToken.MULTIPLICACAO) || 
			verificar(TipoToken.DIVISAO) || 
			verificar(TipoToken.PORCENTAGEM)
			)  {
			TipoToken operador = olhar().tipo;
			avancar();
			No direita = lerPrimario();

			switch(operador) {
				case MULTIPLICACAO:
					esquerda = new NoMultiplicacao(esquerda, direita);
					break;
				case DIVISAO:
					esquerda = new NoDivisao(esquerda, direita);
					break;
				case PORCENTAGEM:
					esquerda = new NoPorcentagem(esquerda, direita);
					break;
			}
		}
		return esquerda;
	}

	private No lerPrimario() {
		if(verificar(TipoToken.PARENTESE_ESQ)) {
			avancar();
			No expressao = lerComparacao();
			consumir(TipoToken.PARENTESE_DIR, "Esperado ')'");
			return expressao;
		}
		return lerTermo();
	}

	private boolean verificarOperadorComparacao() {
		return verificar(TipoToken.IGUAL_IGUAL) ||
			verificar(TipoToken.DIFERENTE) ||
			verificar(TipoToken.MAIOR) ||
			verificar(TipoToken.MENOR) ||
			verificar(TipoToken.MAIOR_IGUAL) ||
			verificar(TipoToken.MENOR_IGUAL);
	}

	private No lerTermo() {
		Token token = olhar();
		if(token.tipo == TipoToken.IDENTIFICADOR) {
			// se for IDENTIFICADOR com "(" trata como chamada de função
			if(olharProximo(1).tipo == TipoToken.PARENTESE_ESQ) {
				String nomeFunc = avancar().valor; // consome IDENTIFICADOR
				avancar(); // consome  "("
				List<No> argumentos = new ArrayList<No>();
				if(!verificar(TipoToken.PARENTESE_DIR)) {
					do {
						argumentos.add(lerComparacao());
					} while(verificar(TipoToken.VIRGULA) && avancar() != null);
				}
				consumir(TipoToken.PARENTESE_DIR, "esperado ')'");
				return new NoChamadaFuncao(nomeFunc, argumentos);
			// tratar acesso a array
			} else if(olharProximo(1).tipo == TipoToken.CONCHETE_ESQ) {
				Token nomeToken = avancar(); // consome IDENTIFICADOR
				No array = new NoValor(nomeToken.valor, nomeToken.tipo);
				while(olhar().tipo == TipoToken.CONCHETE_ESQ) {
					avancar(); // consome '['
					No indice = lerComparacao();
					consumir(TipoToken.CONCHETE_DIR, "esperado ']'");
					array = new NoAcessoArray(array, indice);
				}
				return array;
			}
			// caso contrário, é variavel ou literal
			avancar();
			return new NoValor(token.valor, token.tipo);
		} else if(token.tipo == TipoToken.NUMERO || token.tipo == TipoToken.TEX) {
			avancar();
			return new NoValor(token.valor, token.tipo);
		} else if(verificar(TipoToken.PARENTESE_ESQ)) {
			avancar();
			No expr = lerComparacao();
			consumir(TipoToken.PARENTESE_DIR, "esperado ')'");
			return expr;
		} else if(verificar(TipoToken.CONCHETE_ESQ)) {
			return lerArray();
		}
		System.err.println("token inválido em lerTermo(): " + token.tipo);
		return null;
	}

	// declaracoes
	private No declaracaoRetorne() {
		consumir(TipoToken.RETORNE, "esperado 'retorne'");
		No exprRetorno = lerComparacao(); // le a expressao a retornar
		consumir(TipoToken.PONTO_VIRGULA, "esperado ';' após 'retorne'");
		return new NoRetorne(exprRetorno);
	}
	
	private No declaracaoEnq() {
		consumir(TipoToken.ENQ, "Esperado 'enquanto' ou 'enq'");
		consumir(TipoToken.PARENTESE_ESQ, "Esperado '(' após 'enquanto' ou 'enq'");
		No condicao = lerComparacao();
		consumir(TipoToken.PARENTESE_DIR, "Esperado ')' após condição");
		consumir(TipoToken.CHAVE_ESQ, "Esperado '{' após condição");

		List<No> corpo = new ArrayList<>();
		while(!verificar(TipoToken.CHAVE_DIR)) {
			corpo.add(declaracao());
		}
		consumir(TipoToken.CHAVE_DIR, "Esperado '}'");
		return new NoEnq(condicao, corpo);
	}

	private boolean fim() {
		return atual >= tokens.size();
	}

	private No declaracaoPor() {
		consumir(TipoToken.POR, "Esperado 'por'");
		consumir(TipoToken.PARENTESE_ESQ, "Esperado '(' após 'por'");

		No inicializacao = null;
		if(!verificar(TipoToken.PONTO_VIRGULA)) {
			if(verificar(TipoToken.VARIAVEL)) {
				inicializacao = declaracaoVariavel();
			} else {
				inicializacao = declaracao();
			}
		} else {
			avancar(); // consome ';' vazio
		}

		No condicao = null;
		if(!verificar(TipoToken.PONTO_VIRGULA)) {
			condicao = lerComparacao();
		}
		
		No incremento = null;
		if(!verificar(TipoToken.PARENTESE_DIR)) {
			incremento = lerComparacao();
		}
		consumir(TipoToken.PARENTESE_DIR, "Esperado ')' após incremento");
		consumir(TipoToken.CHAVE_ESQ, "Esperado '{' após ')'");

		List<No> corpo = new ArrayList<No>();
		while(!verificar(TipoToken.CHAVE_DIR) && !fim()) {
			corpo.add(declaracao());
		}
		consumir(TipoToken.CHAVE_DIR, "Esperado '}'");

		return new NoPor(inicializacao, condicao, incremento, corpo);
	}

	private No declaracaoClasse() {
		consumir(TipoToken.CLASSE, "Esperado 'classe'");
		String nome = consumir(TipoToken.IDENTIFICADOR, "Esperado nome da classe").valor;
		consumir(TipoToken.CHAVE_ESQ, "Esperado '{'");

		List<No> membros = new ArrayList<>();
		while(!verificar(TipoToken.CHAVE_DIR)) {
			membros.add(declaracao());
		}
		consumir(TipoToken.CHAVE_DIR, "Esperado '}'");

		return new NoClasse(nome, membros);
	}

	private No declaracaoVariavel() {
        Token tokenTipo = olhar();
        avancar(); // consome token do tipo

        TipoToken tipoDeclarado;
        if(tokenTipo.tipo == TipoToken.VARIAVEL) {
            tipoDeclarado = null;
        } else if(tokenTipo.tipo == TipoToken.IDENTIFICADOR) {
            // nome de classe -> variavel de instância
            tipoDeclarado = TipoToken.INSTANCIA;
        } else {
            tipoDeclarado = tokenTipo.tipo;
        }

        String nome = consumir(TipoToken.IDENTIFICADOR, "esperado nome da variavel").valor;
        consumir(TipoToken.ATRIBUICAO, "esperado '=' após nome da variável");
        No expressao = lerComparacao();
        consumir(TipoToken.PONTO_VIRGULA, "esperado ';' após valor da variavel");

        return new NoAtribuicao(nome, tipoDeclarado, expressao);
    }

    private No declaracaoFuncao() {
        consumir(TipoToken.FUNCAO, "esperado 'func'");
        String nome = consumir(TipoToken.IDENTIFICADOR, "esperado nome da função na declaração").valor;
        consumir(TipoToken.PARENTESE_ESQ, "esperado '('");

        List<String> parametros = new ArrayList<>();
		if(!verificar(TipoToken.PARENTESE_DIR)) {
			do {
				parametros.add(consumir(TipoToken.IDENTIFICADOR, "esperado parâmetro").valor);
				if(verificar(TipoToken.VIRGULA)) {
					avancar(); // consome a virgula se existir
				}
			} while(!verificar(TipoToken.PARENTESE_DIR) && !fim());
		}
        consumir(TipoToken.PARENTESE_DIR, "esperado ')'");
        consumir(TipoToken.CHAVE_ESQ, "esperado '{'");

        List<No> corpo = new ArrayList<>();
        while(!verificar(TipoToken.CHAVE_DIR)) {
            corpo.add(declaracao());
        }
        consumir(TipoToken.CHAVE_DIR, "esperado '}'");

        return new NoFuncao(nome, parametros, corpo);
    }

	private No chamadaFuncao() {
		String nome = consumir(TipoToken.IDENTIFICADOR, "esperado nome da funcao").valor;
		consumir(TipoToken.PARENTESE_ESQ, "esperado '('");

		List<No> argumentos = new ArrayList<>();
		int contadorSeguranca = 0;
		final int MAX_ARGS = 255; // limite seguro de argumentos

		// ler argumentos até encontrar o fechamento de parênteses
		while(!verificar(TipoToken.PARENTESE_DIR) && contadorSeguranca < MAX_ARGS) {
			argumentos.add(lerComparacao());
			contadorSeguranca++;

			// so consome virgula se houver mais argumentos
			if(verificar(TipoToken.VIRGULA)) {
				avancar();
			} else {
				break;
			}
		}
		// verificação de segurança contra loops infinitos
		if(contadorSeguranca >= MAX_ARGS) {
			throw new RuntimeException("número excessivo de argumentos na função: " + nome);
		}

		consumir(TipoToken.PARENTESE_DIR, "esperado ')'");

		// ponto e virgula é opcional
		if(verificar(TipoToken.PONTO_VIRGULA)) {
			avancar();
		}
		return new NoChamadaFuncao(nome, argumentos);
	}

	private No declaracaoCondicional() {
		consumir(TipoToken.SE, "Esperado 'se'");
		consumir(TipoToken.PARENTESE_ESQ, "Esperado '(' após 'se'");
		No condicao = lerComparacao();
		consumir(TipoToken.PARENTESE_DIR, "Esperado ')' após condição");
		consumir(TipoToken.CHAVE_ESQ, "Esperado '{' após condição");

		List<No> blocoSe = new ArrayList<>();
		while(!verificar(TipoToken.CHAVE_DIR)) {
			blocoSe.add(declaracao());
		}
		consumir(TipoToken.CHAVE_DIR, "Esperado '}'");

		List<No> blocoSenao = new ArrayList<>();
		if(verificar(TipoToken.SENAO)) {
			avancar(); // consome "senao"
			if(verificar(TipoToken.CHAVE_ESQ)) {
				consumir(TipoToken.CHAVE_ESQ, "Esperado '{' após 'senao'");
				while(!verificar(TipoToken.CHAVE_DIR)) {
					blocoSenao.add(declaracao());
				}
				consumir(TipoToken.CHAVE_DIR, "Esperado '}'");
			} else {
				blocoSenao.add(declaracao());
			}
		}
		return new NoCondicional(condicao, blocoSe, blocoSenao);
	}

    private Token consumir(TipoToken tipo, String mensagemErro) {
		if(verificar(tipo)) {
			return avancar();
		}
		System.err.println(mensagemErro);
		// Avança para evitar loop infinito
		return avancar();
	}

    private boolean verificar(TipoToken tipo) {
        return !estaNoFim() && olhar().tipo==tipo;
    }

    private Token avancar() {
        if(!estaNoFim()) atual++;
        return anterior();
    }

    private boolean estaNoFim() {
        return olhar().tipo==TipoToken.FIM;
    }

    private Token olhar() {
        return tokens.get(atual);
    }

    private Token anterior() {
        return tokens.get(atual - 1);
    }
}

class Escopo {
    public final Map<String, String> variaveis = new HashMap<>();
    public final Escopo pai;

    public Escopo(Escopo pai) {
        this.pai = pai;
    }

    public void definir(String nome, String valor) {
        variaveis.put(nome, valor);
    }

    public String obter(String nome) {
        if(variaveis.containsKey(nome)) {
            return variaveis.get(nome);
        } else if(pai != null) {
            return pai.obter(nome);
        } else {
            return " vazio"; // variavel nao encontrada
        }
    }
}

class Interpretador {
	private final Map<String, NoClasse> classes = new HashMap<>();
	private int contadorInstancias = 0;
    private final Map<String, ObjetoInstancia> instancias = new HashMap<>();
    private final Map<String, NoFuncao> funcoes = new HashMap<>();
    private Escopo escopoAtual = new Escopo(null); // escopo global

    public void executar(List<No> nos) {
        for(No no : nos) {
            if(no instanceof NoCondicional) {
				NoCondicional cond = (NoCondicional) no;
				boolean resultado = avaliarCondicao(cond.condicao);
				if(resultado) {
					executar(cond.blocoSe);
				} else if(!cond.blocoSenao.isEmpty()) {
					executar(cond.blocoSenao);
				}
			} else if(no instanceof NoFuncao) {
                funcoes.put(((NoFuncao) no).nome, (NoFuncao) no);
            } else if(no instanceof NoAtribuicao) {
                NoAtribuicao atribuicao = (NoAtribuicao) no;
                String valorResolvido = resolverValor(atribuicao.valor);
                escopoAtual.definir(atribuicao.nome, valorResolvido);
			} else if(no instanceof NoChamadaFuncao) {
                executarChamada((NoChamadaFuncao) no);
            } else if(no instanceof NoEnq) {
				NoEnq loop = (NoEnq) no;
				while(avaliarCondicao(loop.condicao)) {
					executar(loop.corpo);
				}
			} else if(no instanceof NoPor) {
				NoPor loop = (NoPor) no;
				if(loop.inicializacao != null) {
					executar(Collections.singletonList(loop.inicializacao));
				}
				while(loop.condicao == null || avaliarCondicao(loop.condicao)) {
					executar(loop.corpo);
					if(loop.incremento != null) {
						resolverValor(loop.incremento);
					}
				}
			} else if(no instanceof NoClasse) {
				NoClasse classe = (NoClasse) no;
				classes.put(classe.nome, classe);
			} else if (no instanceof NoNovo) {
				NoNovo novo = (NoNovo) no;
				String id = "instancia_" + (++contadorInstancias);
				instancias.put(id, criarInstancia(novo.nome));
				escopoAtual.definir(id, id); // Armazena referência no escopo
			} else {
				System.err.println("tipo de nó desconhecido: " + no.getClass().getSimpleName());
            }
        } 
    }
	
	private ObjetoInstancia criarInstancia(String nomeClasse) {
		NoClasse classe = classes.get(nomeClasse);
		if (classe == null) {
			System.err.println("Classe '" + nomeClasse + "' não encontrada.");
			return null;
		}
		return new ObjetoInstancia(nomeClasse, classe);
	}

    // Coletor de lixo mark-and-sweep
    private void coletarLixo() {
        Set<String> marcados = new HashSet<>();

        // Fase de marcação (percorre todas as referências ativas)
        marcarReferencias(escopoAtual, marcados);

        // Fase de limpeza
        Iterator<String> iterator = instancias.keySet().iterator();
		while (iterator.hasNext()) {
			String id = iterator.next();
			if (!marcados.contains(id)) {
				iterator.remove();
			}
		}
    }

    private void marcarReferencias(Escopo escopo, Set<String> marcados) {
        while (escopo != null) {
            for (String valor : escopo.variaveis.values()) {
                if (valor.startsWith("instancia_") && !marcados.contains(valor)) {
                    marcados.add(valor);
                    marcarReferenciasObjeto(valor, marcados);
                }
            }
            escopo = escopo.pai;
        }
    }

    private void marcarReferenciasObjeto(String id, Set<String> marcados) {
        ObjetoInstancia obj = instancias.get(id);
        if (obj == null) return;

        for (String valorCampo : obj.campos.values()) {
            if (valorCampo.startsWith("instancia_") && !marcados.contains(valorCampo)) {
                marcados.add(valorCampo);
                marcarReferenciasObjeto(valorCampo, marcados);
            }
        }
    }

	private boolean avaliarCondicao(No condicao) {
		if(condicao instanceof NoChamadaFuncao) {
			NoIgualIgual op = (NoIgualIgual) condicao;
			String valEsq = resolverValor(op.esquerda);
			String valDir = resolverValor(op.direita);
			return valEsq.equals(valDir);
		}
		if(condicao instanceof NoIgualIgual) {
			NoIgualIgual op = (NoIgualIgual) condicao;
			String valEsq = resolverValor(op.esquerda);
			String valDir = resolverValor(op.direita);
			return valEsq.equals(valDir);
		} else if(condicao instanceof NoDiferente) {
			NoDiferente op = (NoDiferente) condicao;
			String valEsq = resolverValor(op.esquerda);
			String valDir = resolverValor(op.direita);
			return !valEsq.equals(valDir);
		} else if(condicao instanceof NoMaior) {
			NoMaior op = (NoMaior) condicao;
			float valEsq = Float.parseFloat(resolverValor(op.esquerda));
			float valDir = Float.parseFloat(resolverValor(op.direita));
			return valEsq > valDir;
		} else if(condicao instanceof NoMenor) {
			NoMenor op = (NoMenor) condicao;
			float valEsq = Float.parseFloat(resolverValor(op.esquerda));
			float valDir = Float.parseFloat(resolverValor(op.direita));
			return valEsq < valDir;
		} else if(condicao instanceof NoMaiorIgual) {
			NoMaiorIgual op = (NoMaiorIgual) condicao;
			float valEsq = Float.parseFloat(resolverValor(op.esquerda));
			float valDir = Float.parseFloat(resolverValor(op.direita));
			return valEsq >= valDir;
		} else if(condicao instanceof NoMenorIgual) {
			NoMenorIgual op = (NoMenorIgual) condicao;
			float valEsq = Float.parseFloat(resolverValor(op.esquerda));
			float valDir = Float.parseFloat(resolverValor(op.direita));
			return valEsq <= valDir;
		}
		if(condicao instanceof NoChamadaFuncao) {
            String r = resolverValor(condicao);
            if("verdade".equals(r)) return true;
            if("falso".equals(r)) return false;
            try {
                return Double.parseDouble(r) != 0;
            } catch(Exception ex) {
                return !r.isEmpty();
            }
        }
		if(condicao instanceof NoValor) {
            NoValor v = (NoValor) condicao;
            String s = v.valor;
            if(v.tipo == TipoToken.TEX) {
                return !s.isEmpty();
            }
            if(v.tipo == TipoToken.NUMERO) {
                try {
                    return Double.parseDouble(s) != 0;
                } catch(Exception ex) {
                    return false;
                }
            }
            return false;
        }
		return false;
	}

	private String executarNativa(NoChamadaFuncao chamada) {
		String ret = "função desconhecida: " + chamada.nome;
		List<String> args = new ArrayList<String>();
		for(No arg : chamada.argumentos) args.add(resolverValor(arg));
		// comum:
		if("log".equals(chamada.nome)) {
			StringBuilder output = new StringBuilder();
			for(No ar : chamada.argumentos) {
				output.append(resolverValor(ar)).append(" ");
			}
			System.out.println(output.toString().trim());
		} else if("FPexec".equals(chamada.nome)) {
			new FP(args.get(0));
		// mamatica:
		} else if("mPI".equals(chamada.nome)) {
			return "3.141592653589793";
		} else if("mAbs".equals(chamada.nome)) {
			return String.valueOf(Math.abs(Double.parseDouble(args.get(0))));
		} else if("mAleatorio".equals(chamada.nome)) {
			return String.valueOf(Math.random());
		} else if("mSen".equals(chamada.nome)) {
			return String.valueOf(Math.sin(Double.parseDouble(args.get(0))));
		} else if("mCos".equals(chamada.nome)) {
			return String.valueOf(Math.cos(Double.parseDouble(args.get(0))));
		// armazenamento:
		} else if("obExterno".equals(chamada.nome)) {
			return Environment.getExternalStorageDirectory().toString();
		} else if("gravarArquivo".equals(chamada.nome)) {
			ArquivosUtil.escreverArquivo(args.get(0), args.get(1));
		} else if("lerArquivo".equals(chamada.nome)) {
			return ArquivosUtil.lerArquivo(args.get(0));
		} else if("execArquivo".equals(chamada.nome)) {
			new FP(ArquivosUtil.lerArquivo(args.get(0)));
		} else {
			System.err.println(ret);
		}
		return ret;
	}

    private String resolverValor(No no) {
        if(no instanceof NoValor) {
            NoValor v = (NoValor) no;
            if(v.tipo == TipoToken.IDENTIFICADOR) {
                return escopoAtual.obter(v.valor);
            }
            return v.valor;
        }
		
		if(no instanceof NoAcessoArray) {
			NoAcessoArray acesso = (NoAcessoArray) no;
			String arrayStr = resolverValor(acesso.array);

			// Verificar se é uma representação válida de array
			if(arrayStr == null || !arrayStr.startsWith("[") || !arrayStr.endsWith("]")) {
				return "vazio";
			}

			// Extrair conteúdo do array
			String conteudo = arrayStr.substring(1, arrayStr.length()-1);
			String[] elementos;
			if(conteudo.isEmpty()) {
				elementos = new String[0];
			} else {
				elementos = conteudo.split(",");
				// Remover espaços em branco
				for(int i = 0; i < elementos.length; i++) {
					elementos[i] = elementos[i].trim();
				}
			}

			try {
				int indice = Integer.parseInt(resolverValor(acesso.indice));
				if(indice >= 0 && indice < elementos.length) {
					return elementos[indice];
				}
			} catch(NumberFormatException e) {
				// Índice inválido
			}
			return "vazio";
		}
		
		if(no instanceof NoArray) {
			NoArray array = (NoArray) no;
			StringBuilder sb = new StringBuilder("[");
			for(int i = 0; i < array.itens.size(); i++) {
				sb.append(resolverValor(array.itens.get(i)));
				if(i < array.itens.size() - 1) sb.append(",");
			}
			sb.append("]");
			return sb.toString();
		}
		if(no instanceof NoNovo) {
			NoNovo n = (NoNovo) no;
			String id = "instancia_" + (++contadorInstancias);
			ObjetoInstancia obj = criarInstancia(n.nome);
			if(obj == null) return "vazio";
			instancias.put(id, obj);
			// retorna id para atribuição e validação de tipo
			return id;
		}
        if(no instanceof NoChamadaFuncao) {
            return executarFuncaoExpressao((NoChamadaFuncao) no);
        }
        if(no instanceof NoAdicao) {
            NoAdicao n = (NoAdicao) no;
            String e = resolverValor(n.esquerda);
            String d = resolverValor(n.direita);
            try {
                return String.valueOf(Integer.parseInt(e) + Integer.parseInt(d));
            } catch(Exception ex1) {
                try {
                    return String.valueOf(Long.parseLong(e) + Long.parseLong(d));
                } catch(Exception ex2) {
                    try {
                        return String.valueOf(Float.parseFloat(e) + Float.parseFloat(d));
                    } catch(Exception ex3) {
                        try {
                            return String.valueOf(Double.parseDouble(e) + Double.parseDouble(d));
                        } catch(Exception ex4) {
                            return e + d;
                        }
                    }
                }
            }
        }
        if(no instanceof NoSubtracao) {
            NoSubtracao n = (NoSubtracao) no;
            String e = resolverValor(n.esquerda);
            String d = resolverValor(n.direita);
            try {
                return String.valueOf(Integer.parseInt(e) - Integer.parseInt(d));
            } catch(Exception ex1) {
                try {
                    return String.valueOf(Long.parseLong(e) - Long.parseLong(d));
                } catch(Exception ex2) {
                    try {
                        return String.valueOf(Float.parseFloat(e) - Float.parseFloat(d));
                    } catch(Exception ex3) {
                        try {
                            return String.valueOf(Double.parseDouble(e) - Double.parseDouble(d));
                        } catch(Exception ex4) {
                            return "";
                        }
                    }
                }
            }
        }
        if(no instanceof NoMultiplicacao) {
            NoMultiplicacao n = (NoMultiplicacao) no;
            String e = resolverValor(n.esquerda);
            String d = resolverValor(n.direita);
            try {
                return String.valueOf(Integer.parseInt(e) * Integer.parseInt(d));
            } catch(Exception ex1) {
                try {
                    return String.valueOf(Long.parseLong(e) * Long.parseLong(d));
                } catch(Exception ex2) {
                    try {
                        return String.valueOf(Float.parseFloat(e) * Float.parseFloat(d));
                    } catch(Exception ex3) {
                        try {
                            return String.valueOf(Double.parseDouble(e) * Double.parseDouble(d));
                        } catch(Exception ex4) {
                            return "";
                        }
                    }
                }
            }
        }
        if(no instanceof NoDivisao) {
            NoDivisao n = (NoDivisao) no;
            String e = resolverValor(n.esquerda);
            String d = resolverValor(n.direita);
            try {
                return String.valueOf(Integer.parseInt(e) / Integer.parseInt(d));
            } catch(Exception ex1) {
                try {
                    return String.valueOf(Long.parseLong(e) / Long.parseLong(d));
                } catch(Exception ex2) {
                    try {
                        return String.valueOf(Float.parseFloat(e) / Float.parseFloat(d));
                    } catch(Exception ex3) {
                        try {
                            return String.valueOf(Double.parseDouble(e) / Double.parseDouble(d));
                        } catch(Exception ex4) {
                            return "";
                        }
                    }
                }
            }
        }
        if (no instanceof NoPorcentagem) {
            NoPorcentagem n = (NoPorcentagem) no;
            String e = resolverValor(n.esquerda);
            String d = resolverValor(n.direita);
            try {
                return String.valueOf(Integer.parseInt(e) % Integer.parseInt(d));
            } catch (Exception ex1) {
                try {
                    return String.valueOf(Long.parseLong(e) % Long.parseLong(d));
                } catch(Exception ex2) {
                    try {
                        return String.valueOf(Float.parseFloat(e) % Float.parseFloat(d));
                    } catch (Exception ex3) {
                        try {
                            return String.valueOf(Double.parseDouble(e) % Double.parseDouble(d));
                        } catch(Exception ex4) {
                            return "";
                        }
                    }
                }
            }
        }
        return "";
    }
	
	private boolean validarTipo(String valor, TipoToken tipo) {
		try {
			switch(tipo) {
				case BOOL:
					return valor == "verdade" || valor == "falso";
				case INT:
					Integer.parseInt(valor);
					return true;
				case FLUTU:
				case DOBRO:
					Float.parseFloat(valor);
					return true;
				case TEX:
					return true; // qualquer string é valida
				case INSTANCIA:
					return valor != null && valor.startsWith("instancia_");
				default:
					return false;
			}
		} catch(NumberFormatException e) {
			return false;
		}
	}

	private String executarFuncaoExpressao(NoChamadaFuncao chamada) {
		NoFuncao func = funcoes.get(chamada.nome);
		if(func == null) {
			return executarNativa(chamada);
		}
		
		Escopo escopoAnterior = escopoAtual;
		escopoAtual = new Escopo(escopoAnterior);
		// define parâmetros
		for(int i = 0; i < func.parametros.size(); i++) {
			String val = resolverValor(chamada.argumentos.get(i));
			escopoAtual.definir(func.parametros.get(i), val);
		}
		
		String valorRet = "";
		for(No no : func.corpo) {
			if(no instanceof NoRetorne) {
				valorRet = resolverValor(((NoRetorne) no).valor);
				break;
			} else if(no instanceof NoAtribuicao) {
				NoAtribuicao atribuicao = (NoAtribuicao) no;
				String valorResolvido = resolverValor(atribuicao.valor);
				
				// validação de tipo
				if(atribuicao.tipoDeclarado != null) {
					if(!validarTipo(valorResolvido, atribuicao.tipoDeclarado)) {
						System.err.println(
						"erro de tipo: " + atribuicao.nome + 
						" esperava " + atribuicao.tipoDeclarado + 
						", recebeu: " + valorResolvido);
						continue;
					}
				} 
				escopoAtual.definir(atribuicao.nome, valorResolvido);
			} else if(no instanceof NoChamadaFuncao) {
				executarChamada((NoChamadaFuncao) no);
			} else if(no instanceof NoCondicional) {
				NoCondicional cond = (NoCondicional) no;
				boolean res = avaliarCondicao(cond.condicao);
				if(res) {
					for(No sub : cond.blocoSe) {
						if(sub instanceof NoRetorne) {
							valorRet = resolverValor(((NoRetorne) sub).valor);
							break;
						} else if(sub instanceof NoAtribuicao) {
							NoAtribuicao a2 = (NoAtribuicao) sub;
							escopoAtual.definir(a2.nome, resolverValor(a2.valor));
						} else if(sub instanceof NoChamadaFuncao) {
							executarChamada((NoChamadaFuncao) sub);
						}
					}
					if(!valorRet.isEmpty()) break;
				} else {
					for(No sub : cond.blocoSenao) {
						if(sub instanceof NoRetorne) {
							valorRet = resolverValor(((NoRetorne) sub).valor);
							break;
						} else if(sub instanceof NoAtribuicao) {
							NoAtribuicao a2 = (NoAtribuicao) sub;
							escopoAtual.definir(a2.nome, resolverValor(a2.valor));
						} else if(sub instanceof NoChamadaFuncao) {
							executarChamada((NoChamadaFuncao) sub);
						}
					}
					if(!valorRet.isEmpty()) break;
				}
			}
		}
		escopoAtual = escopoAnterior;
		return valorRet;
	}

	private void executarChamada(NoChamadaFuncao chamada) {
		if(funcoes.containsKey(chamada.nome)) {
			executarFuncaoExpressao(chamada);
		} else {
			executarNativa(chamada);
		}
	}
}

class ArquivosUtil {
    private static boolean arquivoExiste(String caminho) {
        File arquivo = new File(caminho);
        return arquivo.exists();
    }

    private static void criarDiretorio(String caminho) {
        if(!arquivoExiste(caminho)) {
            File file = new File(caminho);
            file.mkdirs();
        }
    }
	
    public static void criarNovoArquivo(String caminho) {
        int ultimoPasso= caminho.lastIndexOf(File.separator);
        if(ultimoPasso > 0) {
            String caminhoDiretorio = caminho.substring(0, ultimoPasso);
            criarDiretorio(caminhoDiretorio);
        }
        File arquivo = new File(caminho);
        try {
            if(!arquivo.exists()) arquivo.createNewFile();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static String lerArquivo(String caminho) {
		criarNovoArquivo(caminho);

		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(caminho));
			String linha;
			while((linha = br.readLine()) != null)
			{
				sb.append(linha).append("\n");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

    public static void escreverArquivo(String caminho, String texto) {
        criarNovoArquivo(caminho);
        FileWriter escritor = null;

        try {
            escritor = new FileWriter(new File(caminho), false);
            escritor.write(texto);
            escritor.flush();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(escritor != null)
                    escritor.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}

public class FP {
    public FP(String codigo) {
		try {
			AnalisadorLexico lexico = new AnalisadorLexico(limpar(codigo));
			List<Token> tokens = lexico.tokenizar();
			AnalisadorSintatico sintatico = new AnalisadorSintatico(tokens);
			List<No> nos = sintatico.analisar();
			new Interpretador().executar(nos);
		} catch(Exception e) {
			System.out.println("erro: "+e);
		}
    }
	
	private String limpar(String codigo) {
		codigo = codigo.replaceAll("//.*?$", "");
		codigo = codigo.replaceAll("/\\*(.|\\n)*?\\*/", "");
		codigo = codigo.replaceAll("[ \t]+", " ");
		codigo = codigo.replaceAll("(?m)^\\s*", "");
		codigo = codigo.replaceAll("(?m)^\\s*\\n", "");
		return codigo.trim();
	}
}
