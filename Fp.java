import java.util.*;
import java.io.*;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;

enum TipoToken {
    // tipos:
    STRING,
    NUMERO,
    // operadores:
    ATRIBUICAO,
	ADICAO,
	SUBTRACAO,
	MULTIPLICACAO,
	DIVISAO,
	PORCENTAGEM,
    // variaveis:
    VARIAVEL,
	// metodos
    FUNCAO, 
    // nome:
    IDENTIFICADOR,
    // fecha e abre:
    PARENTESE_ESQ, 
    PARENTESE_DIR, 
    CHAVE_ESQ, 
    CHAVE_DIR,
    // expressoes:
    VIRGULA, 
    PONTO_VIRGULA,
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
            } else if(atual=='"') {
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
            case ';':
                tokens.add(new Token(TipoToken.PONTO_VIRGULA, ";"));
                posicao++;
                return true;
            case ',':
                tokens.add(new Token(TipoToken.VIRGULA, ","));
                posicao++;
                return true;
            case '=':
                tokens.add(new Token(TipoToken.ATRIBUICAO, "="));
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
        valor.equals("func") ? TipoToken.FUNCAO :
        valor.equals("var") ? TipoToken.VARIAVEL :
        TipoToken.IDENTIFICADOR, valor
        );
    }

    private Token lerNumero() {
        buffer.setLength(0);
        while(posicao < codigo.length() && Character.isDigit(codigo.charAt(posicao))) {
            buffer.append(codigo.charAt(posicao));
            posicao++;
        }
        return new Token(TipoToken.NUMERO, buffer.toString());
    }

    private Token lerString() {
        buffer.setLength(0);
        posicao++; // pula aspas inicial
        while(posicao<codigo.length() && codigo.charAt(posicao) != '"') {
            buffer.append(codigo.charAt(posicao));
            posicao++;
        }
        if(posicao>=codigo.length()) {
            System.err.println("string nao fechada");
        }
        posicao++; // pula aspas final
        return new Token(TipoToken.STRING, buffer.toString());
    }
}

interface No {}
    
// operadores:
class NoAtribuicao implements No {
    String nome;
    No valor;

    public NoAtribuicao(String nome, No valor) {
        this.nome = nome;
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
    No esquerda;
    No direita;

    public NoAdicao(No esquerda, No direita) {
        this.esquerda = esquerda;
        this.direita = direita;
    }
}

class NoSubtracao implements No {
    No esquerda;
    No direita;

    public NoSubtracao(No esquerda, No direita) {
        this.esquerda = esquerda;
        this.direita = direita;
    }
}

class NoMultiplicacao implements No {
    No esquerda;
    No direita;

    public NoMultiplicacao(No esquerda, No direita) {
        this.esquerda = esquerda;
        this.direita = direita;
    }
}

class NoDivisao implements No {
    No esquerda;
    No direita;

    public NoDivisao(No esquerda, No direita) {
        this.esquerda = esquerda;
        this.direita = direita;
    }
}

class NoPorcentagem implements No {
    No esquerda;
    No direita;

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
    List<String> argumentos;

    public NoChamadaFuncao(String nome, List<String> argumentos) {
        this.nome = nome;
        this.argumentos = argumentos;
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
       if(verificar(TipoToken.VARIAVEL)) return declaracaoVariavel();
       return chamadaFuncao();
   }

	private No lerExpressao() {
		No esquerda = lerTermo();
		while(verificar(TipoToken.ADICAO)) {
			avancar(); // +
			No direita = lerTermo();
			esquerda = new NoAdicao(esquerda, direita);
		}
		while(verificar(TipoToken.SUBTRACAO)) {
			avancar(); // -
			No direita = lerTermo();
			
			esquerda = new NoSubtracao(esquerda, direita);
		}
		while(verificar(TipoToken.MULTIPLICACAO)) {
			avancar(); // *
			No direita = lerTermo();

			esquerda = new NoMultiplicacao(esquerda, direita);
		}
		while(verificar(TipoToken.DIVISAO)) {
			avancar(); // /
			No direita = lerTermo();

			esquerda = new NoDivisao(esquerda, direita);
		}
		while(verificar(TipoToken.PORCENTAGEM)) {
			avancar(); // %
			No direita = lerTermo();

			esquerda = new NoPorcentagem(esquerda, direita);
		}
		return esquerda;
	}

	private No lerTermo() {
		Token token = olhar();
		if(token.tipo==TipoToken.NUMERO || token.tipo==TipoToken.STRING || token.tipo==TipoToken.IDENTIFICADOR) {
			avancar();
			return new NoValor(token.valor, token.tipo); // detecta tipo do valor
		} else {
			System.err.println("token invalido: " + token.tipo);
			return null;
		}
	}
	
	// declaracoes
	private No declaracaoVariavel() {
		consumir(TipoToken.VARIAVEL, "esperado 'var'");
		String nome = consumir(TipoToken.IDENTIFICADOR, "esperado nome da variavel").valor;
		consumir(TipoToken.ATRIBUICAO, "esperado '=' após nome da variável");
		No expressao = lerExpressao(); // le expressoes complexas
		consumir(TipoToken.PONTO_VIRGULA, "esperado ';' após valor da variavel");
		return new NoAtribuicao(nome, expressao);
	}
    
    private No declaracaoFuncao() {
        consumir(TipoToken.FUNCAO, "esperado 'func'");
        String nome = consumir(TipoToken.IDENTIFICADOR, "esperado nome da função").valor;
        consumir(TipoToken.PARENTESE_ESQ, "esperado '('");
        
        List<String> parametros = new ArrayList<>();
        while(!verificar(TipoToken.PARENTESE_DIR)) {
            parametros.add(consumir(TipoToken.IDENTIFICADOR, "esperado parâmetro").valor);
            if(!verificar(TipoToken.PARENTESE_DIR)) {
                consumir(TipoToken.VIRGULA, "esperado ',' entre parâmetros");
            }
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
        
        List<String> argumentos = new ArrayList<>();
        while(!verificar(TipoToken.PARENTESE_DIR)) {
            argumentos.add(lerArgumento());
            if(!verificar(TipoToken.PARENTESE_DIR)) {
                consumir(TipoToken.VIRGULA, "esperado ',' entre argumentos");
            }
        }
        consumir(TipoToken.PARENTESE_DIR, "esperado ')'");
        consumir(TipoToken.PONTO_VIRGULA, "esperado ';'");
        
        return new NoChamadaFuncao(nome, argumentos);
    }

    private String lerArgumento() {
        if(verificar(TipoToken.STRING)) return consumir(TipoToken.STRING, "").valor;
        if(verificar(TipoToken.NUMERO)) return consumir(TipoToken.NUMERO, "").valor;
        System.err.println("argumento invalido: " + olhar().tipo);
        return "";
    }

    private Token consumir(TipoToken tipo, String mensagemErro) {
        if(verificar(tipo)) return avancar();
        System.err.println(mensagemErro);
        return new Token(TipoToken.FIM, "");
    }

    private boolean verificar(TipoToken tipo) {
        return !estaNoFim() && olhar().tipo == tipo;
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

class Interpretador {
    private final Map<String, NoFuncao> funcoes = new HashMap<>();
    private final Map<String, String> variaveis = new HashMap<>();
    
    public void executar(List<No> nos) {
       for(No no : nos) {
           if(no instanceof NoFuncao) {
               funcoes.put(((NoFuncao) no).nome, (NoFuncao) no);
           } else if(no instanceof NoAtribuicao) {
			   NoAtribuicao atribuicao = (NoAtribuicao) no;
			   String valorResolvido = resolverValor(atribuicao.valor);
			   variaveis.put(atribuicao.nome, valorResolvido);
		   } else if(no instanceof NoChamadaFuncao) {
               executarChamada((NoChamadaFuncao) no);
           } else {
               System.err.println("tipo de nó desconhecido: " + no.getClass().getSimpleName());
           }
       }
   }
   
    private void executarChamada(NoChamadaFuncao chamada) {
        switch(chamada.nome) {
            // funcoes nativas:
            case "log":
            List<String> args = new ArrayList<>();
            for(String arg : chamada.argumentos) {
				if(variaveis.containsKey(arg)) args.add(variaveis.getOrDefault(variaveis.get(arg), variaveis.get(arg)));
                else args.add(variaveis.getOrDefault(arg, arg));
             }
             System.out.println(String.join(" ", args));
             break;
             // arquivos:
             case "criarArquivo":
             ArquivosUtil.escreverArquivo(chamada.argumentos.get(0), chamada.argumentos.get(1));
             break;
             case "lerArquivo":
             ArquivosUtil.lerArquivo(chamada.argumentos.get(0));
             break;
            // se nao for nativa chama uma existente
            default:
            exeFuncCustomi(chamada);
         }
     }

    private void exeFuncCustomi(NoChamadaFuncao chamada) {
        NoFuncao funcao = funcoes.get(chamada.nome);
        if(funcao==null) {
            System.err.println("função não encontrada: "+chamada.nome);
            return;
        }
        
        Map<String, String> ambiente = new HashMap<>();
        for(int i=0; i<funcao.parametros.size(); i++) {
            ambiente.put(funcao.parametros.get(i), chamada.argumentos.get(i));
        }
        
        for(No no : funcao.corpo) {
            if(no instanceof NoChamadaFuncao) {
                executarChamada(resolverArgumentos((NoChamadaFuncao) no, ambiente));
            }
        }
    }
	
	private String resolverValor(No no) {
		if(no instanceof NoValor) {
			NoValor valor = (NoValor) no;
			if(valor.tipo==TipoToken.IDENTIFICADOR) {
				return variaveis.getOrDefault(valor.valor, ""); // busca variavel
			}
			return valor.valor; // retorna valor direto(string ou número)
		} else if(no instanceof NoAdicao) {
			NoAdicao adicao = (NoAdicao) no;
			String val1 = resolverValor(adicao.esquerda);
			String val2 = resolverValor(adicao.direita);
			
			try {
				return String.valueOf(Float.parseFloat(val1) + Float.parseFloat(val2));
			} catch(NumberFormatException b) {
				return val1 + val2; // string + string;
			}
		} else if(no instanceof NoSubtracao) {
			NoSubtracao adicao = (NoSubtracao) no;
			String val1 = resolverValor(adicao.esquerda);
			String val2 = resolverValor(adicao.direita);
			
			try {
				return String.valueOf(Float.parseFloat(val1) - Float.parseFloat(val2));
			} catch(NumberFormatException b) {
				return val1.replace(val2, "");
			}
		} else if(no instanceof NoMultiplicacao) {
			NoMultiplicacao adicao = (NoMultiplicacao) no;
			String val1 = resolverValor(adicao.esquerda);
			String val2 = resolverValor(adicao.direita);
			
			try {
					return String.valueOf(Float.parseFloat(val1) * Float.parseFloat(val2));
			} catch(NumberFormatException b) {
				System.err.println("erro, nao é possivel multiplicar duas strings");
			}
		} else if(no instanceof NoDivisao) {
			NoDivisao adicao = (NoDivisao) no;
			String val1 = resolverValor(adicao.esquerda);
			String val2 = resolverValor(adicao.direita);
			
			try {
				return String.valueOf(Float.parseFloat(val1) / Float.parseFloat(val2));
			} catch(NumberFormatException b) {
				System.err.println("erro, nao é possivel dividir duas strings");
			}
		} else if(no instanceof NoPorcentagem) {
			NoPorcentagem adicao = (NoPorcentagem) no;
			String val1 = resolverValor(adicao.esquerda);
			String val2 = resolverValor(adicao.direita);
			
			try {
				return String.valueOf(Float.parseFloat(val1) % Float.parseFloat(val2));
				} catch(NumberFormatException b) {
					System.err.println("erro, nao é possivel capturar a porcentagem de strings");
				}
			}
		return "";
	}
	
    private NoChamadaFuncao resolverArgumentos(NoChamadaFuncao chamada, Map<String, String> ambiente) {
        List<String> argsResolvidos = new ArrayList<>();
        for(String arg : chamada.argumentos) {
            argsResolvidos.add(ambiente.getOrDefault(arg, arg));
        }
        return new NoChamadaFuncao(chamada.nome, argsResolvidos);
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
    private static void criarNovoArquivo(String caminho) {
        int ultimoPasso= caminho.lastIndexOf(File.separator);
        if(ultimoPasso > 0) {
            String caminhoDiretorio = caminho.substring(0, ultimoPasso);
            criarDiretorio(caminhoDiretorio);
        }
        File arquivo = new File(caminho);
        try {
            if(!arquivo.exists()) arquivo.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String lerArquivo(String caminho) {
        criarNovoArquivo(caminho);

        StringBuilder sb = new StringBuilder();
        FileReader fr = null;
        try {
            fr = new FileReader(new File(caminho));

            char[] buff = new char[1024];

            while((fr.read(buff)) > 0) {
                sb.append(new String(buff, 0, 0));
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if(fr != null) {
                try {
                    fr.close();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
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
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(escritor != null)
                    escritor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

public class Fp {
    public Fp(String codigo) {
        AnalisadorLexico lexico = new AnalisadorLexico(codigo);
        List<Token> tokens = lexico.tokenizar();
        AnalisadorSintatico sintatico = new AnalisadorSintatico(tokens);
        List<No> nos = sintatico.analisar();
        new Interpretador().executar(nos);
    }
}
