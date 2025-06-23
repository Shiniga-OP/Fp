#incluir "utils/Mat";
#incluir "/FIDE/olaMundo.fp";

log("[teste 1]: log(), condicionais e variáveis");

var a = 1;
se(a <= 2) {
	log("a é igual a 2");
} senao se(a > 2) {
	log("a é maior que 2: "+a);
} senao {
	log("a é igual a: "+a);
}

log("[teste 2]: loops");

por(var x = 0; x < 5; x = x + 1) {
   log(x);
}

enq(a < 5) {
	log(a+" na conta");
	a += 1;
}

log("[teste 3]: funções nativas e funções comuns com retorno");

FPexec("
var d = \"aa\";
log(d);
");

func exemplo(arg) {
	retorne arg + arg;
}

log(exemplo(5));

log("[teste 4]: validação de tipos e comentários");

// teste
/*
a janta está pronta
*/

Tex t = "string";
Int inte = 1 * 3; // tesaqs
Flutu te = 1 + 1.2;

log("exemplo"-"e", t, inte, te);

log("[teste 5]: arrays e alteração de diferentes escopos");

var arr = [1, "aaa", 1.5];

log(arr);

Int i = 0;

enq(i < 3) {
	log(arr[i]);
	i += 1;
}

arr[0] += 5;
log(arr[0]);
log(arr.tam);

func testeA1(x) {
	var a = [0, 0, 0, 0];
	log(x.tam);
	i = 0;
	log("reatribuição correta em função: "+i);
	por(Int i = 0; i < x.tam; i = i + 1) {
		x[i] = x[i] * 2;
		log(x[i]);
	}
	por(Int i = 0; i < x.tam; i = i + 1) {
		a[i] = a[i] * 2;
	}
	log("declaração de array em função: "+a);
	retorne x;
}

var t4 = testeA1([0, 1, 3, 3]);
log(t4);

log("[teste 6]: declaração de classes, campos e métodos, instâncias");

classe Pessoa {
	Tex nome = "Anônimo";
	Int idade = 0;
	func teste(a) {
		log("aaaa"+a);
		este.teste2();
		retorne "tudo certo";
	}
	
	func teste2() {
		log("meu nome é: "+este.nome);
	}
}

var p = novo Pessoa();
p.nome = "GLRenderOpenGLES30";
log("nome: "+p.nome);
log("idade: "+p.idade);
p.teste("wunnsw88");
log(p.teste(" aaus"));

log("[teste 7]: classes nativas do Java");

var m = novo Mat();
log(m.cos(2.5));
log(m.sen(7));
log(m.aleatorio());
log("valor de PI: "+m.PI);
log("valor de E: "+m.E);
