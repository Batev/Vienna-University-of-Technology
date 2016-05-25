package ad1.ss16.pa;

import java.util.*;

public class Network {

    private ArrayList<HashSet<Integer>> graph;
    private final int length;
    private int criticalTime;
    private static final int special = -1;

    public Network(int n) {
        this.graph = new ArrayList<>(n);
        this.length = n;
        this.criticalTime = 0;

        for (int i = 0; i < n; i++) {
            this.graph.add(new HashSet<>());
        }
    }

    public ArrayList<HashSet<Integer>> getGraph() {
        return this.graph;
    }

    public int numberOfNodes() {
        return this.length;
    }

    public int numberOfConnections() {
        int count = 0;
        for (int i = 0; i < this.length; i++) {
            count += this.graph.get(i).size();
        }
        return (count / 2);
    }

    public void addConnection(int v, int w) {
        boolean edgeIsValid = (v < this.length) && (v >= 0) &&
                (w < this.length) && (w >= 0) && (w != v) &&
                (!this.graph.get(v).contains(w));
        if (edgeIsValid) {

            this.graph.get(v).add(w);
            this.graph.get(w).add(v);
        }
    }

    public void addAllConnections(int v) {
        for (int i = 0; i < this.length; i++) {
            this.addConnection(v, i);
        }
    }

    public void deleteConnection(int v, int w) {
        boolean edgeIsValid = (v < this.length) && (v >= 0) &&
                (w < this.length) && (w >= 0) && (w != v) &&
                (this.graph.get(v).contains(w));
        if (edgeIsValid) {
            this.graph.get(v).remove(w);
            this.graph.get(w).remove(v);
        }
    }

    public void deleteAllConnections(int v) {
        if (!this.graph.get(v).isEmpty()) {
            for (int i = 0; i < this.length; i++) {
                this.deleteConnection(v, i);
            }
        }
    }

    private void dfsCount(boolean[] visited, int start) {
        visited[start] = true;
        for (int node : this.graph.get(start)) {
            if (!visited[node]) {
                dfsCount(visited, node);
            }
        }
    }

    public int numberOfComponents() {
        boolean[] visited = new boolean[this.length];
        int count = 0;
        for (int i = 0; i < this.graph.size(); i++) {
            if (!visited[i]) {
                count++;
                dfsCount(visited, i);
            }
        }
        return count;
    }

    private boolean dfsCycle(boolean[] visited, int current, int parent) {
        visited[current] = true;
        for (int node : this.graph.get(current)) {
            if (!visited[node]) {
                if (dfsCycle(visited, node, current)) {
                    return true;
                }
            } else if (node != parent) {
                return true;
            }
        }
        return false;
    }

    public boolean hasCycle() {
        boolean[] visited = new boolean[this.length];
        for (int i = 0; i < this.length; i++) {
            if (!visited[i]) {
                if (dfsCycle(visited, i, special)) {
                    return true;
                }
            }
        }
        return false;
    }

    private int bfsLength(boolean[] visited, int start, int end) {
        Queue<Integer> queue = new LinkedList<>();
        queue.add(start);
        queue.add(special);
        visited[start] = true;
        int level = 0;
        while (!queue.isEmpty()) {
            start = queue.remove();
            if (start == special) {
                if (queue.isEmpty()) {
                    return special;
                } else {
                    queue.add(start);
                    level++;
                }
            } else if (start == end) {
                return level;
            } else {
                for (int node : this.graph.get(start)) {
                    if (!visited[node]) {
                        visited[node] = true;
                        queue.add(node);

                    }
                }
            }
        }
        return special;
    }

    public int minimalNumberOfConnections(int start, int end) {
        boolean edgeIsValid = (start < this.length) && (start >= 0) && (end < this.length) && (end >= 0);
        if (edgeIsValid) {
            if (start == end) {
                return 0;
            } else {
                boolean[] visited = new boolean[this.length];
                return bfsLength(visited, start, end);
            }
        }
        return special;
    }

    public List<Integer> criticalNodes() {
        List<Integer> critical = new LinkedList<>();
        int parents[] = new int[this.length]; // array to store the parent of each node

        for (int i = 0; i < this.length; i++) {
            parents[i] = special;
        }

        int visitedTimes[] = new int[this.length];
        int lowTimes[] = new int[this.length];
        boolean visited[] = new boolean[this.length];

        for (int i = 0; i < this.length; i++) {
            if (!visited[i]) {
                criticalDfs(i, critical, visitedTimes, lowTimes, visited, parents);
            }
        }
        return critical;
    }

    private void criticalDfs(int next, List<Integer> critical, int visitedTimes[], int lowTimes[], boolean visited[], int parents[]) {

        int children = 0;
        visited[next] = true;
        visitedTimes[next] = lowTimes[next] = ++criticalTime;
        for (int current : this.graph.get(next)) {
            if (!visited[current]) {
                children++;
                parents[current] = next;
                criticalDfs(current, critical, visitedTimes, lowTimes, visited, parents);
                lowTimes[next] = this.minNumber(lowTimes[next], lowTimes[current]);

                if (parents[next] == special && children > 1 && (!critical.contains(next))) {
                    critical.add(next);
                } else if (parents[next] != special && lowTimes[current] >= visitedTimes[next] && (!critical.contains(next))) {
                    critical.add(next);
                }
            } else if (current != parents[next]) {
                lowTimes[next] = minNumber(lowTimes[next], visitedTimes[current]);
            }
        }
    }

    private int minNumber(int a, int b) {
        return (a <= b) ? a : b;
    }

    public List<Integer> criticalNodesIter() {
        List<Integer> critical = new LinkedList<>();
        int after;
        boolean hasCycle = this.hasCycle();
        for (int i = 0; i < this.length; i++) {
            if ((!hasCycle) && this.graph.get(i).size() > 1) {
                critical.add(i);
            }
            else if(this.graph.get(i).size() > 1) {
                after = this.criticalNumberOfComponents(i, this.graph.get(i));
                if (after > 1) {
                    critical.add(i);
                }
            }
        }
        return critical;
    }

    private int criticalNumberOfComponents(int current, HashSet<Integer> temp) {
        boolean[] visited = new boolean[this.length];
        visited[current] = true;
        int count = 0;
        for(int node : temp) {
            if (!visited[node]) {
                count++;
                if (count > 1) {
                    break;
                }
                else {
                    bfs(visited, node);
                }
            }
        }
        return count;
    }

    private void bfs(boolean[] visited, int start) {
        Queue<Integer> queue = new LinkedList<>();
        queue.add(start);
        visited[start] = true;
        while (!queue.isEmpty()) {
            start = queue.remove();
            for (int w : this.graph.get(start)) {
                if(!visited[w]) {
                    visited[w] = true;
                    queue.add(w);
                }
            }
        }
    }


    public static void main(String[] args) {
        Network test = new Network(13);

        test.addConnection(0, 1);
        test.addConnection(0, 2);
        test.addConnection(0, 6);
        test.addConnection(1, 3);
        test.addConnection(1, 4);
        test.addConnection(2, 4);
        test.addConnection(2, 6);
        test.addConnection(2, 7);
        test.addConnection(3, 4);
        test.addConnection(4, 5);
        test.addConnection(6, 7);
        test.addConnection(8, 9);
        test.addConnection(8, 10);
        test.addConnection(9, 10);
        test.addConnection(10, 11);
        test.addConnection(11, 12);

        System.out.println("numberOfNodes(): " + test.numberOfNodes());
        System.out.println("numberOfConnections(): " + test.numberOfConnections());
        System.out.println("numberOfComponents(): " + test.numberOfComponents());
        System.out.println("hasCycle(): " + test.hasCycle());
        System.out.println("minimalNumberOfConnections(0, 5): " + test.minimalNumberOfConnections(0, 5));
        System.out.println("minimalNumberOfConnections(0, 9): " + test.minimalNumberOfConnections(0, 9));
        System.out.println("criticalNodes(): " + test.criticalNodes());
        System.out.println("criticalNodesIter(): " + test.criticalNodesIter());

        System.out.println("All connections: ");
        for (int i = 0; i < test.numberOfNodes(); i++) {
            for (int number : test.getGraph().get(i)) {
                System.out.println(i + " -> " + number);
            }
        }
    }
}