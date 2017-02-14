package src;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

/**
 * A class for implementing the k-MST using the
 * Branch and Bound technique.
 */
public class KMST extends AbstractKMST {
    private int numNodes;
    private int numEdges;
    private int k;
    private int upperBound;
    private Set<Edge> edges;
    private ArrayList<TreeSet<Edge>> graph;
    private int[] lowerBound;

	/**
	 * Constructor for initialising an instance.
	 * 
	 * @param numNodes The number of the nodes.
	 * @param numEdges The number of the edges.
	 * @param edges The entity of the edges.
	 * @param k The number of the nodes, that should be included in the MST.
	 */
    @SuppressWarnings("unchecked")
    public KMST(Integer numNodes, Integer numEdges, HashSet<Edge> edges, int k) {

        // initialize the global variables
        this.numNodes = numNodes;
        this.numEdges = numEdges;
        this.k = k;
        this.edges = edges;
        this.graph = new ArrayList<TreeSet<Edge>>(this.numNodes);
        this.lowerBound = new int[this.k];
        this.upperBound = Integer.MAX_VALUE;

        // initializes the adjacency list
        for (int i = 0; i < this.numNodes; i++) {
            this.graph.add(new TreeSet<Edge>());
        }

        PriorityQueue<Edge> tempPQ = new PriorityQueue<Edge>(this.numEdges);

        // fills the adjacency list
        for (Edge edge : this.edges) {
            this.graph.get(edge.node1).add(edge);
            this.graph.get(edge.node2).add(edge);
            tempPQ.add(edge);
        }

        // fills the array with the values, which will be used as lower bounds
        this.lowerBound = this.initializeLowerBound(tempPQ);
    }

    /**
	 * Runs a heuristic and then the branch and bound algorithm.
	 */
    @Override
    public void run() {

        // prim's algorithm to smallen the range of the bounds
        for (int i = 0; i < this.numNodes; i++) {
            prim(i);
        }

        // the branch and bound algorithm
        for (int i = 0; i < this.numNodes; i++) {
            branchAndBound(new HashSet<Edge>(), i, 0, 0, 0, new PriorityQueue<>(), new boolean[this.numNodes]);
        }

    }

    private void prim(int node) {

        boolean[] visited = new boolean[numNodes];
        boolean isFirstNode = true;
        PriorityQueue<Edge> minHeap = new PriorityQueue<Edge>();
        Set<Edge> mst = new HashSet<Edge>();
        int count = 0;
        int weight = 0;
        int currentNode;
        int adjacentNode;
        // because it is a TreeMap it will take the edge with the lowest weight
        Edge firstEdge = this.graph.get(node).iterator().next();
        // saves the first node adjacent to the current

        minHeap.add(firstEdge);

        while (!minHeap.isEmpty() && count < this.k) {

            Edge currentEdge = minHeap.poll();

            if (isFirstNode) {
                currentNode = node;
            } else {
                currentNode = visited[currentEdge.node1] ? currentEdge.node2 : currentEdge.node1;
            }

            // the current adjacent node
            adjacentNode = currentEdge.node1 == currentNode ? currentEdge.node2 : currentEdge.node1;

            // checks if with the current edge a cycle occurs
            if (!(visited[currentNode] && visited[adjacentNode])) {
                // mark as visited
                visited[currentNode] = true;
                // counter for the visited nodes
                count++;

                // the edge will be added twice if we skip this condition
                if (!isFirstNode) {
                    mst.add(currentEdge);
                    weight += currentEdge.weight;
                }

                for (Edge edge : this.graph.get(currentNode)) {
                    // the adjacent node
                    int tempNode = edge.node1 == currentNode ? edge.node2 : edge.node1;
                    // add only these edges in the queue, that could give us better results
                    if (!(visited[currentNode] && visited[tempNode]) && !minHeap.contains(edge)) {
                        if (!((edge.weight + weight > this.upperBound))) {
                            minHeap.add(edge);
                        }
                    }
                }
                isFirstNode = false;
            }

        }

        // adds the new solution if such is found
        if (this.k == count && weight < this.upperBound) {
            this.upperBound = weight;
            setSolution(this.upperBound, mst);
        }
    }

    public void branchAndBound(HashSet<Edge> currentEdges, int currentNode, int currentWeight,int countEdges, int countNodes, PriorityQueue<Edge> minHeap, boolean[] discovered) {

        // clones the minHeap
        if (minHeap != null) {
            minHeap = new PriorityQueue<Edge>(minHeap);
        }

        HashSet<Edge> tempEdges = new HashSet<Edge>();

        // adds all the current edges to a temporary HashSet
        if (currentEdges != null) {
            for (Edge elem : currentEdges) {
                tempEdges.add(elem);
            }
        }

        int weight = 0;
        int adjacentNode;
        boolean isNewSolution = false;
        Edge currentEdge;

        // adds all to the current node adjacent edges to the queue
        for (Edge elem : this.graph.get(currentNode)) {
            int temp = currentNode == elem.node1 ? elem.node2 : elem.node1;
            if (!discovered[temp] && !minHeap.contains(elem) &&
                    elem.weight + this.calculateLowerBound(currentWeight, countEdges) < this.upperBound) {

                minHeap.offer(elem);
            }
        }

        while (!minHeap.isEmpty()) {

            currentEdge = minHeap.poll();
            weight = currentWeight + currentEdge.weight;

            // checks if the lower bound is bigger than the upper bound
            // when yes there is no need to continue with the loop
            if (this.calculateLowerBound(weight, countEdges) >= this.upperBound) {
                break;
            }

            // when no we continue with the branching
            else {
                // checks for cycle
                if (!(discovered[currentEdge.node1] && discovered[currentEdge.node2])) {

                    // when no cycle and in bounds we add the edge for a possible solution
                    tempEdges.add(currentEdge);

                    if (discovered[currentEdge.node1]) {
                        adjacentNode = currentEdge.node2;
                        currentNode = currentEdge.node1;
                    } else {
                        adjacentNode = currentEdge.node1;
                        currentNode = currentEdge.node2;
                    }

                    // if first edge we declare both nodes as discovered
                    if (countEdges == 0) {
                        discovered[adjacentNode] = true;
                        discovered[currentNode] = true;
                        countNodes += 2;
                    } else {
                        discovered[adjacentNode] = true;
                        countNodes++;
                    }

                    // when true, we have found a new solution
                    if (countNodes == this.k) {
                        this.upperBound = weight;
                        setSolution(weight,tempEdges);
                        isNewSolution = true;
                    } else {
                        // recursive call
                        branchAndBound(tempEdges, adjacentNode, weight, countEdges + 1, countNodes, minHeap, discovered);

                        // backtrack
                        // clears the set
                        tempEdges.clear();
                        if (currentEdges != null) {
                            for (Edge elem : currentEdges) {
                                tempEdges.add(elem);
                            }
                        }
                    }
                    // clears the nodes
                    if (!isNewSolution) {
                        discovered[adjacentNode] = false;
                        countNodes--;
                        if (countEdges == 0) {
                            discovered[currentNode] = false;
                            countNodes--;
                        }
                    }
                }
            }
        }
    }

    private int calculateLowerBound(int weight, int count) {
        int temp = (this.k - count - 2);
        return temp >= 0 && temp < this.k ? weight + this.lowerBound[temp] : 0;
    }

    private int[] initializeLowerBound(PriorityQueue<Edge> pq) {
        int[] saveArray = new int[this.k];
        PriorityQueue<Edge> tempPQ = new PriorityQueue<Edge>(pq);
        int temp;
        if (!pq.isEmpty()) {
            for (int i = 0; i < this.k; i++) {
                if (i == 0) {
                    saveArray[0] = 0;
                } else {
                    temp = tempPQ.poll().weight;
                    saveArray[i] = saveArray[i - 1] + temp;
                }
            }
        }
        return saveArray;
    }

    /**
     * Main class for testing the program functionality.
     * @param args The program arguments.
     */
    public static void main(String[] args) {

        Edge e1 = new Edge(0, 1, 8);
        Edge e2 = new Edge(0, 6, 14);
        Edge e3 = new Edge(0, 7, 17);
        Edge e4 = new Edge(1, 2, 7);
        Edge e5 = new Edge(1, 8, 26);
        Edge e6 = new Edge(2, 3, 22);
        Edge e7 = new Edge(3, 4, 19);
        Edge e8 = new Edge(3, 9, 6);
        Edge e9 = new Edge(3, 10, 21);
        Edge e10 = new Edge(4, 5, 18);
        Edge e11 = new Edge(5, 6, 27);
        Edge e12 = new Edge(5, 7, 19);
        Edge e13 = new Edge(5, 10, 6);
        Edge e14 = new Edge(6, 8, 19);
        Edge e15 = new Edge(6, 10, 5);
        Edge e16 = new Edge(8, 9, 4);
        Edge e17 = new Edge(8, 10, 28);
        Edge e18 = new Edge(9, 10, 23);

        HashSet set = new HashSet();

        set.add(e1);
        set.add(e2);
        set.add(e3);
        set.add(e4);
        set.add(e5);
        set.add(e6);
        set.add(e7);
        set.add(e8);
        set.add(e9);
        set.add(e10);
        set.add(e11);
        set.add(e12);
        set.add(e13);
        set.add(e14);
        set.add(e15);
        set.add(e16);
        set.add(e17);
        set.add(e18);

        KMST kmst = new KMST(11, 18, set, 4);
        kmst.run();
        Set bS = kmst.getSolution().getBestSolution();

        System.out.println(bS);
    }
}

