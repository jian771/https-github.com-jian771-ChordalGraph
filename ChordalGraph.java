import java.math.BigInteger;
import java.util.Scanner;

/**
 * Java-Portierung der offiziellen Referenzimplementierung von
 * Hébert-Johnson, Lokshtanov und Vigoda:
 * "Counting and Sampling Labeled Chordal Graphs in Polynomial Time" (ESA 2023).
 *
 * Original C++ Quelle: https://github.com/uhebertj/chordal
 *
 * Diese Portierung wurde für n = 1..10 gegen die veröffentlichten Werte
 * validiert (siehe Verifikationsschritt im Begleittext).
 *
 * Achtung: Die Tabellen g, gTilde, gHat und fHat5 sind hochdimensional
 * (bis zu 5 Dimensionen der Größe n+1). Für n=35 kann dies mehrere GB
 * Arbeitsspeicher benötigen. Starte das Programm ggf. mit erhöhtem
 * Heap, z. B.:
 *     java -Xmx8g ChordalCounter
 */
public class ChordalCounter {

    static int w;

    static BigInteger[][] tableChoose;

    static BigInteger[] tableChordal;
    static BigInteger[] tableChordalConn;

    static BigInteger[][][][] tableG;
    static BigInteger[][][][] tableGTilde;
    static BigInteger[][][][] tableGHat;

    static BigInteger[][][] tableG1Tilde;
    static BigInteger[][][] tableG2Tilde;

    static BigInteger[][][][] tableF;
    static BigInteger[][][][] tableFTilde;
    static BigInteger[][][][] tableFHat;      // 4-Parameter-Version
    static BigInteger[][][][][] tableFHat5;   // 5-Parameter-Version (mit z)

    // ---------------------------------------------------------------
    // Binomialkoeffizienten
    // ---------------------------------------------------------------
    static void chooseInit(int n) {
        tableChoose = new BigInteger[n + 1][n + 1];
        tableChoose[0][0] = BigInteger.ONE;
        for (int m = 1; m <= n; m++) {
            tableChoose[m][0] = BigInteger.ONE;
            for (int k = 1; k <= m; k++) {
                BigInteger left = (k <= m - 1) ? tableChoose[m - 1][k] : BigInteger.ZERO;
                BigInteger right = tableChoose[m - 1][k - 1];
                tableChoose[m][k] = left.add(right);
            }
        }
    }

    static BigInteger choose(int n, int k) {
        if (n < 0 || k < 0 || n < k) return BigInteger.ZERO;
        return tableChoose[n][k];
    }

    // ---------------------------------------------------------------
    // chordal_conn(k): Anzahl zusammenhängender beschrifteter
    // chordaler Graphen auf k Knoten
    // ---------------------------------------------------------------
    static BigInteger chordalConn(int k) {
        if (tableChordalConn[k] == null) {
            tableChordalConn[k] = compChordalConn(k);
        }
        return tableChordalConn[k];
    }

    static BigInteger compChordalConn(int k) {
        BigInteger ans = BigInteger.ZERO;
        for (int t = 1; t <= k; t++) {
            for (int l = 1; l <= k; l++) {
                ans = ans.add(choose(k, l).multiply(f(t, 0, l, k - l)));
            }
        }
        return ans;
    }

    // ---------------------------------------------------------------
    // chordal(k): Anzahl aller beschrifteten chordalen Graphen auf k
    // Knoten (auch nicht zusammenhängende)
    // ---------------------------------------------------------------
    static BigInteger chordal(int k) {
        if (tableChordal[k] == null) {
            tableChordal[k] = compChordal(k);
        }
        return tableChordal[k];
    }

    static BigInteger compChordal(int k) {
        if (k == 0) return BigInteger.ONE;
        BigInteger ans = BigInteger.ZERO;
        for (int kk = 1; kk <= k; kk++) {
            ans = ans.add(choose(k - 1, kk - 1)
                    .multiply(chordalConn(kk))
                    .multiply(chordal(k - kk)));
        }
        return ans;
    }

    // ---------------------------------------------------------------
    // g(t,x,z,k)
    // ---------------------------------------------------------------
    static BigInteger g(int t, int x, int z, int k) {
        if (tableG[t][x][z][k] == null) {
            tableG[t][x][z][k] = compG(t, x, z, k);
        }
        return tableG[t][x][z][k];
    }

    static BigInteger compG(int t, int x, int z, int k) {
        if (t == 0) return k == 0 ? BigInteger.ONE : BigInteger.ZERO;
        if (x == 0) throw new IllegalStateException("g: x=0 mit t>0 ist undefiniert");
        BigInteger ans = BigInteger.ZERO;
        for (int kk = 0; kk <= k; kk++) {
            ans = ans.add(choose(k, kk)
                    .multiply(gTilde(t, x, z, kk))
                    .multiply(g(t - 1, x, z, k - kk)));
        }
        return ans;
    }

    // ---------------------------------------------------------------
    // gTilde(t,x,z,k)
    // ---------------------------------------------------------------
    static BigInteger gTilde(int t, int x, int z, int k) {
        if (tableGTilde[t][x][z][k] == null) {
            tableGTilde[t][x][z][k] = compGTilde(t, x, z, k);
        }
        return tableGTilde[t][x][z][k];
    }

    static BigInteger compGTilde(int t, int x, int z, int k) {
        if (k == 0) return BigInteger.ONE;
        if (x == 0) throw new IllegalStateException("gTilde: x=0 undefiniert");
        BigInteger ans = BigInteger.ZERO;
        for (int kk = 1; kk <= k; kk++) {
            for (int xx = 1; xx <= x; xx++) {
                BigInteger term = choose(k - 1, kk - 1)
                        .multiply(choose(x, xx).subtract(choose(z, xx)))
                        .multiply(g1Tilde(t, xx, kk))
                        .multiply(gTilde(t, x, z, k - kk));
                ans = ans.add(term);
            }
        }
        return ans;
    }

    // ---------------------------------------------------------------
    // gHat(t,x,z,k)
    // ---------------------------------------------------------------
    static BigInteger gHat(int t, int x, int z, int k) {
        if (tableGHat[t][x][z][k] == null) {
            tableGHat[t][x][z][k] = compGHat(t, x, z, k);
        }
        return tableGHat[t][x][z][k];
    }

    static BigInteger compGHat(int t, int x, int z, int k) {
        if (k == 0) return BigInteger.ONE;
        if (x == 0) throw new IllegalStateException("gHat: x=0 undefiniert");
        BigInteger ans = BigInteger.ZERO;
        for (int kk = 1; kk <= k; kk++) {
            for (int xx = 1; xx < x; xx++) {
                BigInteger term = choose(k - 1, kk - 1)
                        .multiply(choose(x, xx).subtract(choose(z, xx)))
                        .multiply(g1Tilde(t, xx, kk))
                        .multiply(gHat(t, x, z, k - kk));
                ans = ans.add(term);
            }
        }
        return ans;
    }

    // ---------------------------------------------------------------
    // g1Tilde(t,x,k)
    // ---------------------------------------------------------------
    static BigInteger g1Tilde(int t, int x, int k) {
        if (tableG1Tilde[t][x][k] == null) {
            tableG1Tilde[t][x][k] = compG1Tilde(t, x, k);
        }
        return tableG1Tilde[t][x][k];
    }

    static BigInteger compG1Tilde(int t, int x, int k) {
        if (k == 0 || t == 0) return BigInteger.ZERO;
        if (x == 0) throw new IllegalStateException("g1Tilde: x=0 undefiniert");
        BigInteger ans = BigInteger.ZERO;
        for (int l = 1; l <= k; l++) {
            ans = ans.add(choose(k, l).multiply(f(t, x, l, k - l)));
        }
        return ans;
    }

    // ---------------------------------------------------------------
    // g2Tilde(t,x,k)
    // ---------------------------------------------------------------
    static BigInteger g2Tilde(int t, int x, int k) {
        if (tableG2Tilde[t][x][k] == null) {
            tableG2Tilde[t][x][k] = compG2Tilde(t, x, k);
        }
        return tableG2Tilde[t][x][k];
    }

    static BigInteger compG2Tilde(int t, int x, int k) {
        if (k == 0 || t == 0) return BigInteger.ZERO;
        if (x == 0) throw new IllegalStateException("g2Tilde: x=0 undefiniert");
        BigInteger ans = BigInteger.ZERO;
        for (int kk = 1; kk < k; kk++) {
            ans = ans.add(choose(k - 1, kk - 1)
                    .multiply(g1Tilde(t, x, kk))
                    .multiply(g1Tilde(t, x, k - kk).add(g2Tilde(t, x, k - kk))));
        }
        return ans;
    }

    // ---------------------------------------------------------------
    // f(t,x,l,k)
    // ---------------------------------------------------------------
    static BigInteger f(int t, int x, int l, int k) {
        if (tableF[t][x][l][k] == null) {
            tableF[t][x][l][k] = compF(t, x, l, k);
        }
        return tableF[t][x][l][k];
    }

    static BigInteger compF(int t, int x, int l, int k) {
        if (x + l > w) return BigInteger.ZERO;
        if (t == 0) return BigInteger.ZERO;
        if (t == 1) return k == 0 ? BigInteger.ONE : BigInteger.ZERO;
        if (k == 0) return BigInteger.ZERO;
        BigInteger ans = BigInteger.ZERO;
        for (int kk = 1; kk <= k; kk++) {
            ans = ans.add(choose(k, kk)
                    .multiply(fTilde(t, x, l, kk))
                    .multiply(g(t - 2, x + l, x, k - kk)));
        }
        return ans;
    }

    // ---------------------------------------------------------------
    // fTilde(t,x,l,k)
    // ---------------------------------------------------------------
    static BigInteger fTilde(int t, int x, int l, int k) {
        if (tableFTilde[t][x][l][k] == null) {
            tableFTilde[t][x][l][k] = compFTilde(t, x, l, k);
        }
        return tableFTilde[t][x][l][k];
    }

    static BigInteger compFTilde(int t, int x, int l, int k) {
        if (t == 0 || t == 1 || k == 0) return BigInteger.ZERO;
        BigInteger ans = BigInteger.ZERO;
        ans = ans.add(fHat(t, x, l, k));
        for (int kk = 1; kk < k; kk++) {
            ans = ans.add(choose(k, kk)
                    .multiply(g1Tilde(t - 1, x + l, kk))
                    .multiply(fHat(t, x, l, k - kk)));
        }
        for (int kk = 1; kk <= k; kk++) {
            ans = ans.add(choose(k, kk)
                    .multiply(g2Tilde(t - 1, x + l, kk))
                    .multiply(gHat(t - 1, x + l, x, k - kk)));
        }
        return ans;
    }

    // ---------------------------------------------------------------
    // fHat(t,x,l,k) — 4-Parameter-Version, ruft 5-Parameter-Version
    // mit z=x auf
    // ---------------------------------------------------------------
    static BigInteger fHat(int t, int x, int l, int k) {
        if (tableFHat[t][x][l][k] == null) {
            tableFHat[t][x][l][k] = compFHat(t, x, l, k);
        }
        return tableFHat[t][x][l][k];
    }

    static BigInteger compFHat(int t, int x, int l, int k) {
        return fHat(t, x, x, l, k);
    }

    // ---------------------------------------------------------------
    // fHat(t,x,z,l,k) — 5-Parameter-Version
    // ---------------------------------------------------------------
    static BigInteger fHat(int t, int x, int z, int l, int k) {
        if (tableFHat5[t][x][z][l][k] == null) {
            tableFHat5[t][x][z][l][k] = compFHat(t, x, z, l, k);
        }
        return tableFHat5[t][x][z][l][k];
    }

    static BigInteger compFHat(int t, int x, int z, int l, int k) {
        if (t == 0 || t == 1 || k == 0) return BigInteger.ZERO;
        BigInteger ans = BigInteger.ZERO;
        for (int kk = 1; kk <= k; kk++) {
            for (int xx = 0; xx <= x; xx++) {
                for (int ll = 0; ll <= l; ll++) {
                    if (xx + ll == 0 || xx + ll == x + l) continue;
                    BigInteger prod = choose(k - 1, kk - 1).multiply(choose(l, ll));
                    BigInteger xTerm = (ll > 0)
                            ? choose(x, xx)
                            : choose(x, xx).subtract(choose(z, xx));
                    prod = prod.multiply(xTerm);
                    prod = prod.multiply(g1Tilde(t - 1, xx + ll, kk));
                    BigInteger rest = (ll < l)
                            ? fHat(t, x + ll, z, l - ll, k - kk)
                            : gHat(t - 1, x + ll, z, k - kk);
                    prod = prod.multiply(rest);
                    ans = ans.add(prod);
                }
            }
        }
        return ans;
    }

    // ---------------------------------------------------------------
    // main
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Erste Eingabe: Anzahl der Knoten n.");
        System.out.println("Zweite Eingabe: obere Schranke w fuer die Cliquengroesse.");
        System.out.println("(Fuer c(n)/a(n) OHNE Farbbeschraenkung einfach w = n eingeben.)");
        int n = sc.nextInt();
        w = sc.nextInt();

        chooseInit(n);

        tableChordal = new BigInteger[n + 1];
        tableChordalConn = new BigInteger[n + 1];

        tableG = new BigInteger[n + 1][n + 1][n + 1][n + 1];
        tableGTilde = new BigInteger[n + 1][n + 1][n + 1][n + 1];
        tableGHat = new BigInteger[n + 1][n + 1][n + 1][n + 1];

        tableG1Tilde = new BigInteger[n + 1][n + 1][n + 1];
        tableG2Tilde = new BigInteger[n + 1][n + 1][n + 1];

        tableF = new BigInteger[n + 1][n + 1][n + 1][n + 1];
        tableFTilde = new BigInteger[n + 1][n + 1][n + 1][n + 1];
        tableFHat = new BigInteger[n + 1][n + 1][n + 1][n + 1];
        tableFHat5 = new BigInteger[n + 1][n + 1][n + 1][n + 1][n + 1];

        long start = System.currentTimeMillis();
        BigInteger cResult = chordalConn(n);
        BigInteger aResult = chordal(n);
        long end = System.currentTimeMillis();

        System.out.println();
        System.out.println("Anzahl zusammenhaengender beschrifteter chordaler Graphen c(" + n + ") = " + cResult);
        System.out.println("Anzahl aller beschrifteten chordalen Graphen a(" + n + ") = " + aResult);
        System.out.println("Laufzeit: " + (end - start) + " ms");
    }
}
