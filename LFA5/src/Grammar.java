import java.util.*;

class Grammar {
    private Map<String, List<String>> P;
    private List<String> V_N;
    private List<String> V_T;

    public Grammar() {
        P = new HashMap<>();
        P.put("S", Arrays.asList("dB", "A"));
        P.put("A", Arrays.asList("d", "dS", "aAdAB"));
        P.put("B", Arrays.asList("aC", "aS", "AC"));
        P.put("C", Arrays.asList("eps", "aA"));
        P.put("E", Arrays.asList("AS"));

        V_N = new ArrayList<>(Arrays.asList("S", "A", "B", "C", "E"));
        V_T = new ArrayList<>(Arrays.asList("a", "d"));
    }

    public Map<String, List<String>> RemoveEpsilon() {
        // Step 1: Remove epsilon productions
        List<String> nt_epsilon = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : P.entrySet()) {
            String s = entry.getKey();
            List<String> productions = entry.getValue();
            if (productions.contains("eps")) {
                nt_epsilon.add(s);
            }
        }

        for (Map.Entry<String, List<String>> entry : P.entrySet()) {
            String key = entry.getKey();
            List<String> value = entry.getValue();
            List<String> newProductions = new ArrayList<>(value);
            for (String ep : nt_epsilon) {
                List<String> toAdd = new ArrayList<>();
                for (String v : value) {
                    if (v.contains(ep)) {
                        toAdd.add(v.replace(ep, ""));
                    }
                }
                newProductions.addAll(toAdd);
            }
            P.put(key, newProductions);
        }

        Map<String, List<String>> P1 = new HashMap<>(P);
        for (Map.Entry<String, List<String>> entry : P.entrySet()) {
            List<String> value = entry.getValue();
            value.removeIf(v -> v.equals("eps"));
        }

        Map<String, List<String>> P_final = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : P1.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                P_final.put(entry.getKey(), entry.getValue());
            } else {
                V_N.remove(entry.getKey());
            }
        }

        System.out.println("1. After removing epsilon productions:\n" + P_final);
        P = new HashMap<>(P_final);
        return P_final;
    }

    public Map<String, List<String>> EliminateUnitProd() {
        // Step 2: Eliminate unit productions
        Map<String, List<String>> P2 = new HashMap<>(P);
        for (Map.Entry<String, List<String>> entry : P.entrySet()) {
            String key = entry.getKey();
            List<String> value = entry.getValue();
            List<String> newProductions = new ArrayList<>();
            for (String v : value) {
                if (v.length() == 1 && V_N.contains(v)) {
                    newProductions.addAll(P.get(v));
                } else {
                    newProductions.add(v);
                }
            }
            P2.put(key, newProductions);
        }
        System.out.println("2. After removing unit productions:\n" + P2);
        P = new HashMap<>(P2);
        return P2;
    }

    public Map<String, List<String>> EliminateInaccesible() {
        // Step 3: Eliminate inaccessible symbols
        Map<String, List<String>> P3 = new HashMap<>(P);
        Set<String> accessibleSymbols = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add("S");

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (!accessibleSymbols.contains(current)) {
                accessibleSymbols.add(current);
                List<String> productions = P.get(current);
                if (productions != null) {
                    for (String production : productions) {
                        for (char symbol : production.toCharArray()) {
                            if (Character.isUpperCase(symbol) && !accessibleSymbols.contains(String.valueOf(symbol))) {
                                queue.add(String.valueOf(symbol));
                            }
                        }
                    }
                }
            }
        }

        P3.keySet().retainAll(accessibleSymbols);

        System.out.println("3. After removing inaccessible symbols:\n" + P3);
        System.out.println(V_N);
        P = new HashMap<>(P3);
        return P3;
    }

    public Map<String, List<String>> RemoveUnprod() {
        // Step 4: Remove unproductive symbols
        Map<String, List<String>> P4 = new HashMap<>(P);
        Set<String> productiveSymbols = new HashSet<>();
        boolean changed = true;

        while (changed) {
            changed = false;
            for (Map.Entry<String, List<String>> entry : P.entrySet()) {
                String key = entry.getKey();
                List<String> value = entry.getValue();
                for (String v : value) {
                    boolean productive = true;
                    for (char c : v.toCharArray()) {
                        if (Character.isUpperCase(c) && !productiveSymbols.contains(String.valueOf(c))) {
                            productive = false;
                            break;
                        }
                    }
                    if (productive) {
                        if (!productiveSymbols.contains(key)) {
                            productiveSymbols.add(key);
                            changed = true;
                        }
                    }
                }
            }
        }

        P4.keySet().retainAll(productiveSymbols);

        for (Map.Entry<String, List<String>> entry : P.entrySet()) {
            List<String> value = entry.getValue();
            value.removeIf(v -> v.chars().anyMatch(c -> Character.isUpperCase(c) && !productiveSymbols.contains(String.valueOf((char) c))));
        }

        System.out.println("4. After removing unproductive symbols:\n" + P4);
        P = new HashMap<>(P4);
        return P4;
    }

    public Map<String, List<String>> TransformToCNF() {
        // Step 5: Transform to CNF
        Map<String, List<String>> P5 = new HashMap<>(P);
        Map<String, String> temp = new HashMap<>();
        List<String> vocabulary = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");
        List<String> freeSymbols = new ArrayList<>(vocabulary);
        freeSymbols.removeAll(P.keySet());

        for (Map.Entry<String, List<String>> entry : P.entrySet()) {
            String key = entry.getKey();
            List<String> value = entry.getValue();

            List<String> newProductions = new ArrayList<>();
            for (String v : value) {
                if ((v.length() == 1 && V_T.contains(v)) || (v.length() == 2 && v.chars().allMatch(Character::isUpperCase))) {
                    newProductions.add(v);
                } else {
                    String left = v.substring(0, v.length() / 2);
                    String right = v.substring(v.length() / 2);

                    String tempKey1 = temp.containsValue(left) ? temp.entrySet().stream().filter(e -> e.getValue().equals(left)).map(Map.Entry::getKey).findFirst().orElse(null) : freeSymbols.remove(0);
                    if (!temp.containsValue(left)) temp.put(tempKey1, left);
                    String tempKey2 = temp.containsValue(right) ? temp.entrySet().stream().filter(e -> e.getValue().equals(right)).map(Map.Entry::getKey).findFirst().orElse(null) : freeSymbols.remove(0);
                    if (!temp.containsValue(right)) temp.put(tempKey2, right);

                    newProductions.add(tempKey1 + tempKey2);
                }
            }
            P5.put(key, newProductions);
        }

        for (Map.Entry<String, String> entry : temp.entrySet()) {
            P5.put(entry.getKey(), Collections.singletonList(entry.getValue()));
        }

        System.out.println("5. Final CNF:\n" + P5);
        return P5;
    }

    public void ReturnProductions() {
        System.out.println("Initial Grammar:\n" + P);
        Map<String, List<String>> P1 = RemoveEpsilon();
        Map<String, List<String>> P2 = EliminateUnitProd();
        Map<String, List<String>> P3 = EliminateInaccesible();
        Map<String, List<String>> P4 = RemoveUnprod();
        Map<String, List<String>> P5 = TransformToCNF();
    }

    public static void main(String[] args) {
        Grammar g = new Grammar();
        g.ReturnProductions();
    }
}