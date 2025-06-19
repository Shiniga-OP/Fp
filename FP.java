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
	INCLUIR,
	MARCACAO,
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
			case '#':
				tokens.add(new Token(TipoToken.MARCACAO, "#"));
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
			valor.equals("incluir") ? TipoToken.INCLUIR :
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

class NoAtribuicaoArray implements No {
    No array;
    No indice;
    No valor;

    public NoAtribuicaoArray(No array, No indice, No valor) {
        this.array = array;
        this.indice = indice;
        this.valor = valor;
    }
}

class NoAtribuicaoPropriedade implements No {
    No objeto;
    String propriedade;
    No valor;
    public NoAtribuicaoPropriedade(No objeto, String propriedade, No valor) {
        this.objeto = objeto;
        this.propriedade = propriedade;
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

class NoAcessoPropriedade implements No {
    No objeto;
    String propriedade;

    public NoAcessoPropriedade(No objeto, String propriedade) {
        this.objeto = objeto;
        this.propriedade = propriedade;
    }
}

// importação:
class NoIncluir implements No {
    final String caminho;

    public NoIncluir(String caminho) {
        this.caminho = caminho;
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

class NoChamadaMetodo implements No {
    No objeto;
    String nome;
    List<No> argumentos;

    public NoChamadaMetodo(No objeto, String nome, List<No> argumentos) {
        this.objeto = objeto;
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
		if(verificar(TipoToken.MARCACAO)) return declaracaoIncluir();
		
		// chamada dr netodo
		if (olhar().tipo == TipoToken.IDENTIFICADOR
            && olharProximo(1).tipo == TipoToken.PONTO
            && olharProximo(2).tipo == TipoToken.IDENTIFICADOR
            && olharProximo(3).tipo == TipoToken.PARENTESE_ESQ) {

            Token objetoToken = avancar(); // objeto
            avancar(); // consome '.'
            Token metodoToken = avancar(); // nome do método
            avancar(); // consome '('

            List<No> argumentos = new ArrayList<>();
            if (!verificar(TipoToken.PARENTESE_DIR)) {
                do {
                    argumentos.add(lerComparacao());
                } while (verificar(TipoToken.VIRGULA) && avancar() != null);
            }
            consumir(TipoToken.PARENTESE_DIR, "Esperado ')' após argumentos");
            consumir(TipoToken.PONTO_VIRGULA, "Esperado ';'");

            return new NoChamadaMetodo(
                new NoValor(objetoToken.valor, objetoToken.tipo),
                metodoToken.valor,
                argumentos
            );
        }
		
		//verificação pra reatribuição
		if((olhar().tipo == TipoToken.IDENTIFICADOR || olhar().tipo == TipoToken.ESTE)
		   && olharProximo(1).tipo == TipoToken.PONTO
		   && olharProximo(2).tipo == TipoToken.IDENTIFICADOR
		   && olharProximo(3).tipo == TipoToken.ATRIBUICAO) {
			Token objetoToken = avancar();
			No objeto = new NoValor(objetoToken.valor, objetoToken.tipo);
			avancar(); // consome '.'
			String prop = consumir(TipoToken.IDENTIFICADOR,"").valor;
			avancar(); // consome '='
			No val = lerComparacao();
			consumir(TipoToken.PONTO_VIRGULA,"");
			return new NoAtribuicaoPropriedade(objeto, prop, val);
		}
		if(olhar().tipo == TipoToken.IDENTIFICADOR && 
		   olharProximo(1).tipo == TipoToken.CONCHETE_ESQ) {
			Token nome = avancar();
			avancar(); // consome '['
			No indice = lerComparacao();
			consumir(TipoToken.CONCHETE_DIR, "Esperado ']'");
			consumir(TipoToken.ATRIBUICAO, "Esperado '=' após índice");
			No valor = lerComparacao();
			consumir(TipoToken.PONTO_VIRGULA, "Esperado ';' após atribuição");
			return new NoAtribuicaoArray(new NoValor(nome.valor, nome.tipo), indice, valor);
		}
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
			case NOVO:
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
		if(verificar(TipoToken.NOVO)) {
			avancar(); // consome 'novo'
			String nomeClasse = consumir(TipoToken.IDENTIFICADOR, "esperado nome da classe").valor;
			if(verificar(TipoToken.PARENTESE_ESQ)) {
				avancar();
				consumir(TipoToken.PARENTESE_DIR, "esperado ')'");
			}
			return new NoNovo(nomeClasse);
		}
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
		// acesso a campos em classes com "este"
		if(token.tipo == TipoToken.ESTE && olharProximo(1).tipo == TipoToken.PONTO) {
			avancar(); // consome 'este'
			avancar(); // consome '.'
			String propriedade = consumir(TipoToken.IDENTIFICADOR, "esperado nome da propriedade").valor;
			return new NoAcessoPropriedade(new NoValor("este", TipoToken.IDENTIFICADOR), propriedade);
		}
		if(token.tipo == TipoToken.IDENTIFICADOR) {
			// chamada de metodos
			if(olharProximo(1).tipo == TipoToken.PONTO && olharProximo(2).tipo == TipoToken.IDENTIFICADOR && olharProximo(3).tipo == TipoToken.PARENTESE_ESQ) {
				No alvo = new NoValor(avancar().valor, TipoToken.IDENTIFICADOR);
				avancar(); // '.'
				String nomeM = consumir(TipoToken.IDENTIFICADOR,"").valor;
				avancar(); // '('
				List<No> args = new ArrayList<>();
				if (!verificar(TipoToken.PARENTESE_DIR)) {
					do { args.add(lerComparacao()); }
					while (verificar(TipoToken.VIRGULA) && avancar()!=null);
				}
				consumir(TipoToken.PARENTESE_DIR,"");
				return new NoChamadaMetodo(alvo, nomeM, args);
			}
			// chamadas de funções normais
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
			// acesso de itens de arrays
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
				// acesso a propriedades;
			} else if(olharProximo(1).tipo == TipoToken.PONTO) {
				Token objetoToken = avancar(); // consome o identificador
				avancar(); // consome o ponto
				Token propriedadeToken = consumir(TipoToken.IDENTIFICADOR, "Esperado nome da propriedade após '.'");
				return new NoAcessoPropriedade(new NoValor(objetoToken.valor, objetoToken.tipo), propriedadeToken.valor);
			}
			// caso contrario, é variavel ou literal
			avancar();
			return new NoValor(token.valor, token.tipo);
		} else if(token.tipo == TipoToken.NUMERO || token.tipo == TipoToken.TEX) {
			avancar();
			return new NoValor(token.valor, token.tipo);
			// prioridade de contas com parenteses
		} else if(verificar(TipoToken.PARENTESE_ESQ)) {
			avancar();
			No expr = lerComparacao();
			consumir(TipoToken.PARENTESE_DIR, "esperado ')'");
			return expr;
			// declara arrays
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
		
		consumir(TipoToken.PONTO_VIRGULA, "Esperado ';' após condicao");

		No incremento = null;
		if(!verificar(TipoToken.PARENTESE_DIR)) {
			if(verificar(TipoToken.IDENTIFICADOR) && olharProximo(1).tipo == TipoToken.ATRIBUICAO) {
				Token nome = avancar(); // IDENTIFICADOR
				avancar(); // =
				No valor = lerComparacao();
				incremento = new NoAtribuicao(nome.valor, null, valor);
			} else {
				incremento = lerComparacao();
			}
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
			// declarações de variaveis(campos) e funções metodos)
			if(verificar(TipoToken.VARIAVEL) || 
			   verificar(TipoToken.BOOL) || 
			   verificar(TipoToken.TEX) || 
			   verificar(TipoToken.INT) || 
			   verificar(TipoToken.FLUTU) || 
			   verificar(TipoToken.DOBRO) ||
			   verificar(TipoToken.FUNCAO)) 
			{
				membros.add(declaracao());
			} else {
				System.err.println("Membro inválido na classe: " + olhar().tipo);
				avancar(); // pula token invalido
			}
		}
		consumir(TipoToken.CHAVE_DIR, "Esperado '}'");
		return new NoClasse(nome, membros);
	}
	
	private No declaracaoIncluir() {
		consumir(TipoToken.MARCACAO, "esperado '#'");
        consumir(TipoToken.INCLUIR, "esperado 'incluir'");
        String caminho = consumir(TipoToken.TEX, "esperado nome da variavel").valor;
        consumir(TipoToken.PONTO_VIRGULA, "esperado ';' após inclusão");

        return new NoIncluir(caminho);
    }

	private No declaracaoVariavel() {
        Token tokenTipo = olhar();
        avancar(); // consome token do tipo

        TipoToken tipoDeclarado;
		if(tokenTipo.tipo == TipoToken.IDENTIFICADOR) {
            // nome de classe = variavel de instancia
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
		// avanca para evitar loop infinito
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
    static class Variavel {
        public final TipoToken tipoDeclarado;
        public String valor;

        public Variavel(TipoToken tipoDeclarado, String valor) {
            this.tipoDeclarado = tipoDeclarado;
            this.valor = valor;
        }
    }

    public final Map<String, Variavel> variaveis = new HashMap<>();
    public final Escopo pai;

    public Escopo(Escopo pai) {
        this.pai = pai;
    }

    public void definir(String nome, TipoToken tipoDeclarado, String valor) {
        Variavel var = new Variavel(tipoDeclarado, valor);
        variaveis.put(nome, var);
    }

    public void atribuir(String nome, String valor) {
		if(!temNoEscopo(nome) && !variaveis.containsKey(nome)) definir(nome, TipoToken.VARIAVEL, valor);
			
        if(temNoEscopo(nome)) {
			Variavel var =	obterEscopo(nome).variaveis.get(nome);
			if(var != null) {
				var.valor = valor;
			}
		} else {
			Variavel var = encontrarVariavel(nome);
			if(var != null) {
				var.valor = valor;
			}
        } 
    }

    public String obter(String nome) {
        Variavel var = encontrarVariavel(nome);
        return (var != null) ? var.valor : "vazio";
    }
	
	private boolean temNoEscopo(String nome) {
		return obterEscopo(nome) != null;
	}

	private Escopo obterEscopo(String nome) {
		for(Escopo e = this; e != null; e = e.pai) {
			if(e.variaveis.containsKey(nome)) return e;
		}
		return null;
	}

    public TipoToken obterTipoDeclarado(String nome) {
        Variavel var = encontrarVariavel(nome);
        return (var != null) ? var.tipoDeclarado : null;
    }

    // Implementação do método encontrarVariavel
    private Variavel encontrarVariavel(String nome) {
        for(Escopo escopo = this; escopo != null; escopo = escopo.pai) {
            if(escopo.variaveis.containsKey(nome)) {
                return escopo.variaveis.get(nome);
            }
        }
        return null;
    }
}

class Interpretador {
	private final Map<String, NoClasse> classes = new HashMap<>();
	private int contadorInstancias = 0;
    private final Map<String, ObjetoInstancia> instancias = new HashMap<>();
    private final Map<String, NoFuncao> funcoes = new HashMap<>();
    private Escopo escopoAtual = new Escopo(null); // escopo global
	
	// execuções:
	// globais:
    public void executar(List<No> nos) {
        for(No no : nos) {
            if(no instanceof NoIncluir) {
				NoIncluir i = (NoIncluir) no;
				AnalisadorLexico lexico = new AnalisadorLexico(FP.limpar(ArquivosUtil.lerArquivo(Environment.getExternalStorageDirectory()+i.caminho)));
				List<Token> tokens = lexico.tokenizar();
				AnalisadorSintatico sintatico = new AnalisadorSintatico(tokens);
				List<No> noss = sintatico.analisar();
				executar(noss);
			}else if(no instanceof NoCondicional) {
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
				NoAtribuicao a = (NoAtribuicao) no;
				String v = resolverValor(a.valor);

				if(a.tipoDeclarado != null) {
					// declaração inicial
					if(!avaliarTipo(v, a.tipoDeclarado)) {
						System.err.println("Erro de tipo: " + a.nome + " esperava " + a.tipoDeclarado + ", recebeu: " + v);
					}
					escopoAtual.definir(a.nome, a.tipoDeclarado, v);
				} else {
					// reatribuição
					TipoToken tipoOriginal = escopoAtual.obterTipoDeclarado(a.nome);
					if(tipoOriginal != null && !avaliarTipo(v, tipoOriginal)) {
						System.err.println("Erro de tipo: " + a.nome + " esperava " + tipoOriginal + ", recebeu: " + v);
					}
					escopoAtual.atribuir(a.nome, v);
				}
			} else if(no instanceof NoChamadaFuncao) {
                executarChamada((NoChamadaFuncao) no);
            } else if(no instanceof NoEnq) {
				NoEnq loop = (NoEnq) no;
				while(avaliarCondicao(loop.condicao)) {
					executar(loop.corpo);
				}
			} else if(no instanceof NoPor) {
				NoPor loop = (NoPor) no;

				// executa inicialização
				if(loop.inicializacao != null) {
					executar(Collections.singletonList(loop.inicializacao));
				}

				// loop principal
				while(loop.condicao == null || avaliarCondicao(loop.condicao)) {
					// executa corpo do loop
					for(No sub : loop.corpo) {
						executar(Collections.singletonList(sub));
					}

					// executar incremento
					if(loop.incremento != null) {
						executar(Collections.singletonList(loop.incremento));
					}
				}
			} else if (no instanceof NoAtribuicaoArray) {
				NoAtribuicaoArray atribArray = (NoAtribuicaoArray) no;
				String nomeArray = ((NoValor) atribArray.array).valor;
				String arrayStr = escopoAtual.obter(nomeArray);

				// converte string do array para lista
				List<String> lista = new ArrayList<>();
				if(arrayStr.startsWith("[") && arrayStr.endsWith("]")) {
					String conteudo = arrayStr.substring(1, arrayStr.length() - 1);
					if(!conteudo.isEmpty()) {
						String[] elementos = conteudo.split(",");
						for(String elemento : elementos) {
							lista.add(elemento.trim());
						}
					}
				}

				// atualizar elemento
				int indice = Integer.parseInt(resolverValor(atribArray.indice));
				String novoValor = resolverValor(atribArray.valor);

				if(indice >= 0 && indice < lista.size()) {
					lista.set(indice, novoValor);

					// converte lista de volta para string
					StringBuilder sb = new StringBuilder("[");
					for(int i = 0; i < lista.size(); i++) {
						sb.append(lista.get(i));
						if(i < lista.size() - 1) sb.append(",");
					}
					sb.append("]");
					
					escopoAtual.atribuir(nomeArray, sb.toString());
				}
			} else if(no instanceof NoClasse) {
				NoClasse c = (NoClasse) no;
				classes.put(c.nome, c);
			} else if(no instanceof NoAtribuicaoPropriedade) {
				NoAtribuicaoPropriedade a = (NoAtribuicaoPropriedade) no;
				String id = resolverValor(a.objeto);
				ObjetoInstancia obj = instancias.get(id);
				obj.definirCampo(a.propriedade, resolverValor(a.valor));
			} else if(no instanceof NoChamadaMetodo) {
				NoChamadaMetodo cm = (NoChamadaMetodo) no;
				String id = resolverValor(cm.objeto);
				ObjetoInstancia obj = instancias.get(id);
				if(obj == null) {
					System.err.println("Objeto não encontrado: " + id);
					continue;
				}
				NoClasse def = obj.definicao;
				for(No m : def.membros) {
					if(m instanceof NoFuncao && ((NoFuncao) m).nome.equals(cm.nome)) {
						NoFuncao f = (NoFuncao) m;
						Escopo anterior = escopoAtual;
						escopoAtual = new Escopo(anterior);
						escopoAtual.definir("este", TipoToken.TEX, id);
						for(int i = 0; i < f.parametros.size(); i++) {
							escopoAtual.definir(
								f.parametros.get(i),
								TipoToken.VARIAVEL,
								resolverValor(cm.argumentos.get(i))
							);
						}
						executarBloco(f.corpo);
						escopoAtual = anterior;
						break;
					}
				}
			} else {
				System.err.println("tipo de nó desconhecido: " + no.getClass().getSimpleName());
            }
        } 
    }
	
	// com escopos:
	private String executarFuncaoExpressao(NoChamadaFuncao chamada) {
		NoFuncao func = funcoes.get(chamada.nome);
		if(func == null) return executarNativa(chamada);

		Escopo escopoAnterior = escopoAtual;
		escopoAtual = new Escopo(escopoAnterior);
		for(int i = 0; i < func.parametros.size(); i++)
			escopoAtual.atribuir(func.parametros.get(i), resolverValor(chamada.argumentos.get(i)));

		String valorRet = "";
		for(No no : func.corpo) {
			if(no instanceof NoRetorne) {
				valorRet = resolverValor(((NoRetorne) no).valor);
				break;
			}
			if(no instanceof NoAtribuicao) {
				NoAtribuicao a = (NoAtribuicao) no;
				String v = resolverValor(a.valor);

				if(a.tipoDeclarado != null) {
					// declaração inicial
					if(!avaliarTipo(v, a.tipoDeclarado)) {
						System.err.println("Erro de tipo: " + a.nome + " esperava " + a.tipoDeclarado + ", recebeu: " + v);
					}
					escopoAtual.definir(a.nome, a.tipoDeclarado, v);
				} else {
					// reatribuição
					TipoToken tipoOriginal = escopoAtual.obterTipoDeclarado(a.nome);
					if(tipoOriginal != null && !avaliarTipo(v, tipoOriginal)) {
						System.err.println("Erro de tipo: " + a.nome + " esperava " + tipoOriginal + ", recebeu: " + v);
					}
					escopoAtual.atribuir(a.nome, v);
				}
			}
			if(no instanceof NoAtribuicaoArray) {
				NoAtribuicaoArray a = (NoAtribuicaoArray) no;
				String nome = ((NoValor) a.array).valor;
				String arrayStr = escopoAtual.obter(nome);
				List<String> lista = new ArrayList<String>();
				if(arrayStr.startsWith("[") && arrayStr.endsWith("]")) {
					String[] partes = arrayStr.substring(1, arrayStr.length()-1).split(",");
					for(String p : partes) lista.add(p.trim());
				}
				int idc = Integer.parseInt(resolverValor(a.indice));
				if(idc >= 0 && idc < lista.size()) {
					lista.set(idc, resolverValor(a.valor));
					escopoAtual.atribuir(nome, "[" + String.join(",", lista) + "]");
				}
				continue;
			}
			if(no instanceof NoChamadaFuncao) {
				executarChamada((NoChamadaFuncao) no);
				continue;
			}
			if(no instanceof NoCondicional) {
				NoCondicional cond = (NoCondicional) no;
				List<No> bloco = avaliarCondicao(cond.condicao) ? cond.blocoSe : cond.blocoSenao;
				for(No sub : bloco) {
					if(sub instanceof NoRetorne) {
						valorRet = resolverValor(((NoRetorne) sub).valor);
						break;
					}
					executar(bloco);
				}
				if(!valorRet.isEmpty()) break;
				continue;
			}
			if(no instanceof NoEnq) {
				NoEnq loop = (NoEnq) no;
				while(avaliarCondicao(loop.condicao))
					executar(loop.corpo);
				continue;
			}
			if(no instanceof NoPor) {
				NoPor loop = (NoPor) no;
				// executa inicialização
				if(loop.inicializacao != null) {
					executar(Collections.singletonList(loop.inicializacao));
				}
				// loop principal
				while(loop.condicao == null || avaliarCondicao(loop.condicao)) {
					// executa corpo do loop
					for(No sub : loop.corpo) {
						executar(Collections.singletonList(sub));
					}
					// executar incremento
					if(loop.incremento != null) {
						executar(Collections.singletonList(loop.incremento));
					}
				}
			}
		}
		escopoAtual = escopoAnterior;
		return valorRet;
	}
	
	// nativas:
	private String executarNativa(NoChamadaFuncao chamada) {
		String ret = "função desconhecida: " + chamada.nome;
		List<String> args = new ArrayList<String>();
		for(No arg : chamada.argumentos) args.add(resolverValor(arg));
		// comum:
		if("log".equals(chamada.nome)) {
			StringBuilder saida = new StringBuilder();
			for(No ar : chamada.argumentos) {
				saida.append(resolverValor(ar)).append(" ");
			}
			System.out.println(saida.toString().trim());
			saida = null;
		} else if("liberar".equals(chamada.nome)) {
			for(String arg : args) {
				instancias.remove(arg);
			}
		} else if("proceTempoMilis".equals(chamada.nome)) {
			return String.valueOf(System.currentTimeMillis());
		} else {
			return APIs.executar(chamada, args);
		}
		return ret;
	}
	
	private String executarBloco(List<No> blocos) {
		String valorRetorno = "";
		for(No no : blocos) {
			if(no instanceof NoRetorne) {
				return resolverValor(((NoRetorne) no).valor);
			}
			executar(Collections.singletonList(no));
		}
		return valorRetorno;
	}

	private void executarChamada(NoChamadaFuncao chamada) {
		if(funcoes.containsKey(chamada.nome)) {
			executarFuncaoExpressao(chamada);
		} else {
			executarNativa(chamada);
		}
	}
	
	// fase de testes:
	// orientação a objetos:
	private ObjetoInstancia criarInstancia(String nomeClasse) {
        NoClasse classe = classes.get(nomeClasse);
        if(classe == null) {
            System.err.println("Classe '" + nomeClasse + "' não encontrada.");
            return null;
        }
        ObjetoInstancia obj = new ObjetoInstancia(nomeClasse, classe);
        for(No membro : classe.membros) {
            if(membro instanceof NoAtribuicao) {
                NoAtribuicao campo = (NoAtribuicao) membro;
                String valor = resolverValor(campo.valor);
                obj.definirCampo(campo.nome, valor);
            }
        }
        return obj; // instancia
    }

    private void marcarReferenciasObjeto(String id, Set<String> marcados) {
        ObjetoInstancia obj = instancias.get(id);
        if(obj == null) return;

        for(String valorCampo : obj.campos.values()) {
            if(valorCampo.startsWith("instancia_") && !marcados.contains(valorCampo)) {
                marcados.add(valorCampo);
                marcarReferenciasObjeto(valorCampo, marcados);
            }
        }
    }

	// validações:
	// verifica condições:
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
	// avalia tipos de variaveis:
	private boolean avaliarTipo(String valor, TipoToken tipo) {
		try {
			switch(tipo) {
				case VARIAVEL:
					return true;
				case BOOL:
					return valor.equals("verdade") || valor.equals("falso");
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
	// resolvedores:
	// resolve operações mamaticas:
    private String resolverValor(No no) {
        if(no instanceof NoValor) {
            NoValor v = (NoValor) no;
            if(v.tipo == TipoToken.IDENTIFICADOR) {
                return escopoAtual.obter(v.valor);
            }
            return v.valor;
        }
		
		if(no instanceof NoAcessoPropriedade) {
			NoAcessoPropriedade acesso = (NoAcessoPropriedade) no;
			String objetoStr = resolverValor(acesso.objeto);
			String propriedade = acesso.propriedade;
			
			// se for instancia
			if(objetoStr.startsWith("instancia_")) {
				ObjetoInstancia obj = instancias.get(objetoStr);
				if(obj != null) {
					return obj.obterCampo(propriedade);
				}
			}

			// verifica se é um array e o campo é "tam"
			if(objetoStr.startsWith("[") && objetoStr.endsWith("]")) {
				if("tam".equals(propriedade)) {
					String conteudo = objetoStr.substring(1, objetoStr.length() - 1).trim();
					if(conteudo.isEmpty()) {
						return "0";
					}
					String[] elementos = conteudo.split(",");
					return String.valueOf(elementos.length);
				}
			}
			return "vazio";
		}
		
		if(no instanceof NoAcessoArray) {
			NoAcessoArray acesso = (NoAcessoArray) no;
			String arrayStr = escopoAtual.obter(((NoValor) acesso.array).valor);

			// extrai conteudo do array
			List<String> elementos = new ArrayList<>();
			if(arrayStr.startsWith("[") && arrayStr.endsWith("]")) {
				String conteudo = arrayStr.substring(1, arrayStr.length() - 1);
				if(!conteudo.isEmpty()) {
					String[] parts = conteudo.split(",");
					for(String part : parts) {
						elementos.add(part.trim());
					}
				}
			}

			try {
				int indice = Integer.parseInt(resolverValor(acesso.indice));
				if(indice >= 0 && indice < elementos.size()) {
					return elementos.get(indice);
				}
			} catch(NumberFormatException e) {
				System.out.println("indice invalido");
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
			return id;
		}
		if(no instanceof NoChamadaMetodo) {
			NoChamadaMetodo cm = (NoChamadaMetodo) no;
			String id = resolverValor(cm.objeto);
			ObjetoInstancia obj = instancias.get(id);
			if(obj == null) return "vazio";
			NoClasse def = obj.definicao;
			for(No m : def.membros) {
				if(m instanceof NoFuncao && ((NoFuncao) m).nome.equals(cm.nome)) {
					NoFuncao f = (NoFuncao) m;
					Escopo anterior = escopoAtual;
					escopoAtual = new Escopo(anterior);
					escopoAtual.definir("este", TipoToken.TEX, id);
					for(int i = 0; i < f.parametros.size(); i++) {
						escopoAtual.definir(
							f.parametros.get(i),
							TipoToken.VARIAVEL,
							resolverValor(cm.argumentos.get(i))
						);
					}
					String valorRet = executarBloco(f.corpo);
					escopoAtual = anterior;
					return valorRet;
				}
			}
			return "vazio";
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
        if(no instanceof NoPorcentagem) {
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
}

// API s nativas:
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

// executor:
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
	
	public static String limpar(String codigo) {
		codigo = codigo.replaceAll("(?m)//.*", ""); // remove comentários de linha
		codigo = codigo.replaceAll("(?s)/\\*.*?\\*/", ""); // remove comentários de bloco
		codigo = codigo.replaceAll("[ \t]+", " "); // espaços e mais de 1 espaço
		codigo = codigo.replaceAll("(?m)^\\s+", ""); // remove espaços à esquerda
		codigo = codigo.replaceAll("(?m)^\\s*$\\n?", ""); // remove linhas em branco
		return codigo.trim();
	}
}
