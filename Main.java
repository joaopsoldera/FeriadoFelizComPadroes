import java.util.ArrayList;
import java.util.List;

// ESTRATEGY
interface EstrategiaTaxa {
    double calcular(double valor);
}

class TaxaPoupanca implements EstrategiaTaxa {
    public double calcular(double valor) {
        return valor * 0.02;
    }
}

class TaxaCorrente implements EstrategiaTaxa {
    public double calcular(double valor) {
        return valor * 0.05;
    }
}

// OBSERVER
interface Observador {
    void atualizar(String msg);
}

class AlertaSMS implements Observador {
    public void atualizar(String msg) {
        System.out.println("[SMS] " + msg);
    }
}

// INTERFACE CONTA
interface Conta {
    void depositar(double valor);
    void sacar(double valor);
    double getSaldo();
    String getDescricao();
}

class ContaBasica implements Conta {
    private double saldo;
    private EstrategiaTaxa estrategia;
    private List<Observador> observadores = new ArrayList<>();

    public ContaBasica(EstrategiaTaxa estrategia) {
        this.estrategia = estrategia;
    }

    public void adicionarObservador(Observador o) {
        observadores.add(o);
    }

    public void depositar(double valor) {
        saldo += valor;
        notificar("Depósito de R$" + valor);
    }

    public void sacar(double valor) {
        double total = valor + estrategia.calcular(valor);
        if (saldo >= total) {
            saldo -= total;
            notificar("Saque de R$" + valor);
        } else {
            System.out.println("Saldo insuficiente");
        }
    }

    private void notificar(String msg) {
        for (Observador o : observadores) {
            o.atualizar(msg);
        }
    }

    public double getSaldo() {
        return saldo;
    }

    public String getDescricao() {
        return "Conta básica";
    }
}

// DECORATOR
abstract class DecoradorConta implements Conta {
    protected Conta conta;

    public DecoradorConta(Conta conta) {
        this.conta = conta;
    }

    public void depositar(double valor) {
        conta.depositar(valor);
    }

    public void sacar(double valor) {
        conta.sacar(valor);
    }

    public double getSaldo() {
        return conta.getSaldo();
    }
}

class SeguroConta extends DecoradorConta {

    public SeguroConta(Conta conta) {
        super(conta);
    }

    public void sacar(double valor) {
        double taxaExtra = 5; // custo fixo do seguro
        conta.sacar(valor + taxaExtra);
    }

    public String getDescricao() {
        return conta.getDescricao() + " + seguro";
    }
}

// PROXY
class ProxyConta implements Conta {
    private Conta contaReal;
    private String senha;

    public ProxyConta(Conta conta, String senha) {
        this.contaReal = conta;
        this.senha = senha;
    }

    private boolean autenticar() {
        return "1234".equals(senha);
    }

    public void depositar(double valor) {
        if (autenticar()) contaReal.depositar(valor);
        else System.out.println("Acesso negado");
    }

    public void sacar(double valor) {
        if (autenticar()) contaReal.sacar(valor);
        else System.out.println("Acesso negado");
    }

    public double getSaldo() {
        return contaReal.getSaldo();
    }

    public String getDescricao() {
        return contaReal.getDescricao();
    }
}

// ADAPTER
interface SistemaPagamento {
    void processar(double valor);
}

class SistemaLegado {
    public void executar(double valor) {
        System.out.println("Sistema antigo processando: R$" + valor);
    }
}

class AdaptadorSistema implements SistemaPagamento {
    private SistemaLegado legado = new SistemaLegado();

    public void processar(double valor) {
        legado.executar(valor);
    }
}

// FACTORY
class FabricaConta {
    public static ContaBasica criarConta(String tipo) {
        if (tipo.equalsIgnoreCase("POUPANCA")) {
            return new ContaBasica(new TaxaPoupanca());
        }
        return new ContaBasica(new TaxaCorrente());
    }
}

// SINGLETON
class GerenteBanco {
    private static GerenteBanco instancia;

    private GerenteBanco() {}

    public static GerenteBanco getInstancia() {
        if (instancia == null) {
            instancia = new GerenteBanco();
        }
        return instancia;
    }

    public void log(String msg) {
        System.out.println("[LOG] " + msg);
    }
}

// FACADE
class FachadaBanco {
    private GerenteBanco gerente = GerenteBanco.getInstancia();
    private SistemaPagamento pagamento = new AdaptadorSistema();

    public void executar() {
        gerente.log("Iniciando sistema");

        ContaBasica conta = FabricaConta.criarConta("POUPANCA");
        conta.adicionarObservador(new AlertaSMS());

        Conta contaDecorada = new SeguroConta(conta);
        Conta proxy = new ProxyConta(contaDecorada, "1234");

        proxy.depositar(1000);
        proxy.sacar(100);

        pagamento.processar(50);

        System.out.println("Saldo final: " + proxy.getSaldo());
    }
}

public class Main {
    public static void main(String[] args) {

        System.out.println("TESTE 1: Conta Poupança com Seguro e Proxy");
        ContaBasica conta1 = FabricaConta.criarConta("POUPANCA");
        conta1.adicionarObservador(new AlertaSMS());

        Conta contaSegura = new SeguroConta(conta1);
        Conta proxyValido = new ProxyConta(contaSegura, "1234");

        proxyValido.depositar(1000);
        proxyValido.sacar(100);

        System.out.println("Saldo final: " + proxyValido.getSaldo());


        System.out.println("\nTESTE 2: Tentativa com senha incorreta");
        Conta proxyInvalido = new ProxyConta(contaSegura, "9999");

        proxyInvalido.depositar(500);
        proxyInvalido.sacar(50);


        System.out.println("\nTESTE 3: Conta Corrente (taxa diferente)");
        ContaBasica conta2 = FabricaConta.criarConta("CORRENTE");
        conta2.adicionarObservador(new AlertaSMS());

        conta2.depositar(1000);
        conta2.sacar(100);

        System.out.println("Saldo final: " + conta2.getSaldo());


        System.out.println("\nTESTE 4: Saque com saldo insuficiente");
        ContaBasica conta3 = FabricaConta.criarConta("POUPANCA");

        conta3.depositar(50);
        conta3.sacar(100);


        System.out.println("\nTESTE 5: Adapter");
        SistemaPagamento pagamento = new AdaptadorSistema();
        pagamento.processar(200);


        System.out.println("\nTESTE 6: Singleton");
        GerenteBanco g1 = GerenteBanco.getInstancia();
        GerenteBanco g2 = GerenteBanco.getInstancia();

        g1.log("Primeiro log");
        g2.log("Segundo log");

        System.out.println("Mesma instância? " + (g1 == g2));


        System.out.println("\nTESTE 7: Facade");
        FachadaBanco banco = new FachadaBanco();
        banco.executar();
    }
}