package de.glmtk.experiments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.glmtk.utils.ArrayUtils;
import de.glmtk.utils.StringUtils;

/**
 * Didn't work.
 */
public class E05_GlmGenComb {

    private static List<String> getPatternedHistory(
            List<String> history,
            boolean[] pattern) {
        List<String> result = new ArrayList<String>(history);
        for (int i = 0; i != pattern.length; ++i) {
            if (pattern[i]) {
                result.set(i, "*");
            }
        }
        return result;
    }

    private static boolean nextPattern(boolean[] pattern) {
        int first = 0, last = pattern.length;
        for (int i = last - 2; i != first - 1; --i) {
            if (pattern[i] && !pattern[i + 1]) { // pattern[i] > pattern[ii]
                int j = last - 1;
                while (!pattern[i] || pattern[j]) { // pattern[i] <= pattern[ii]
                    --j;
                }
                ArrayUtils.swap(pattern, i, j);
                ArrayUtils.reverse(pattern, i + 1, last);
                return true;
            }
        }
        ArrayUtils.reverse(pattern, first, last);
        for (int i = 0; i != pattern.length; ++i) {
            if (!pattern[i]) {
                pattern[i] = true;
                return true;
            }
        }
        return false;
    }

    private static long factorial(long n) {
        return n <= 1 ? 1 : n * factorial(n - 1);
    }

    private static int numOrder(int n, int order) {
        return (int) (factorial(n) / (factorial(order) * factorial(n - order)));
    }

    private static List<List<String>> alphas;

    private static List<List<List<List<String>>>> gammasss;

    private static void genComb(String sequence, List<String> history) {
        int numAlphas = (int) Math.pow(2, history.size());
        alphas = new ArrayList<List<String>>(numAlphas);
        gammasss = new ArrayList<List<List<List<String>>>>(numAlphas);
        for (int i = 0; i != numAlphas; ++i) {
            alphas.add(null);
            gammasss.add(new ArrayList<List<List<String>>>());
        }

        boolean[] pattern = new boolean[history.size()];
        for (int i = 0; i != pattern.length; ++i) {
            pattern[i] = false;
        }

        int order = 0;
        int numOrder = numOrder(history.size(), order);
        int orderEnd = numOrder;

        for (int i = 0, iInOrder = 0; i != numAlphas; ++i, ++iInOrder) {
            if (i >= orderEnd) {
                ++order;
                iInOrder = 0;
                numOrder = numOrder(history.size(), order);
                orderEnd += numOrder;
            }
            List<String> h = getPatternedHistory(history, pattern);
            List<String> alpha = ArrayUtils.unionWithSingleton(h, sequence);
            alphas.set(i, alpha);
            for (int j = orderEnd; j != numAlphas; ++j) {
                //                gammasss.get(j).add(alpha);
                if (i == 0) {
                    gammasss.get(j).add(Arrays.asList(alpha));
                    continue;
                }
                if (iInOrder == 0) {
                    List<List<List<String>>> gammass = gammasss.get(j);
                    List<List<List<String>>> newGammass =
                            new ArrayList<List<List<String>>>(gammass.size()
                                    * numOrder);
                    for (int k = 0; k != gammass.size(); ++k) {
                        List<List<String>> gammas = gammass.get(k);
                        for (int l = 0; l != numOrder; ++l) {
                            newGammass.add(new ArrayList<List<String>>(gammas));
                        }
                    }
                    gammasss.set(j, newGammass);
                }
                gammasss.get(j).get(iInOrder).add(alpha);
            }
            nextPattern(pattern);
        }
    }

    public static void main(String[] args) {
        genComb("d", Arrays.asList("a", "b", "c"));

        for (int i = 0; i != alphas.size(); ++i) {
            List<String> alpha = alphas.get(i);
            List<List<List<String>>> gammass = gammasss.get(i);

            String alphaStr =
                    alpha == null ? "null" : StringUtils.join(alpha, " ");
            System.out.println("α(" + alphaStr + ")");
            if (gammass != null) {
                for (List<List<String>> gammas : gammass) {
                    if (gammas != null) {
                        for (List<String> gamma : gammas) {
                            String gammaStr =
                                    gamma == null ? "null" : StringUtils.join(
                                            gamma, " ");
                            System.out.print("  γ(" + gammaStr + ")");
                        }
                        System.out.println();
                    }
                }
            }
        }
    }

}
