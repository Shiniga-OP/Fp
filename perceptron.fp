#incluir "utils/Mat";

var m = novo Mat();

var pesos = [ m.aleatorio() * 2 - 1, m.aleatorio() * 2 - 1 ];
Flutu bias = m.aleatorio() * 2 - 1;
Flutu taxaAprendizado = 0.5;

log(pesos);

func degrau(x) {
    se(x >= 0) {
        retorne 1;
     }
     retorne 0;
}

func prever(entrada) {
    Flutu soma = bias;
    por(Int i = 0; i < entrada.tam; i = i + 1) {
        soma += entrada[i] * pesos[i];
     }
     retorne degrau(soma);
}

func treinar(entrada, saidaEsperada) {
    Int saida = prever(entrada);
    Int erro = saidaEsperada - saida;
    
    por(Int i = 0; i < pesos.tam; i = i + 1) {
        pesos[i] += taxaAprendizado * erro * entrada[i];
     }
     bias += taxaAprendizado * erro;
}

por(Int epoca = 0; epoca < 10; epoca = epoca + 1) {
    treinar([0, 1], 0);
    treinar([1, 0], 0);
    treinar([0, 0], 0);
    treinar([1, 1], 1);
}

log(prever([0, 1]));
log(prever([1, 0]));
log(prever([0, 0]));
log(prever([1, 1]));
log(pesos);
