package it.unibz.gangOf3.util.security.RSALab;

public class RSAKeys {

    private int e;
    private int d;
    private int n;

    public RSAKeys(int e, int d, int n) {
        this.e = e;
        this.d = d;
        this.n = n;
    }

    public int getE() {
        return e;
    }

    public int getD() {
        return d;
    }

    public int getN() {
        return n;
    }
}
