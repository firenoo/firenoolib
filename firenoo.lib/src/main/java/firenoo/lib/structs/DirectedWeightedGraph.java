package firenoo.lib.structs;

import java.util.Set;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;

/**
 * A directed graph data structure. This graph implementation is not
 * thread-safe. Running multiple graph algorithms is not supported.
 * @param <E> type of data that will this graph will hold
 */
public class DirectedWeightedGraph<E> {

    //Marks
    public static final int REVERSE_EDGES_OFS = 0;
    public static final int REVERSE_EDGES_SIZE = 1;
    public static final int BFS_OFS = 1;
    public static final int BFS_SIZE = 1;

    public static final int DFS_VISITED_OFS = 3;
    public static final int DFS_VISITED_SIZE = 1;
    
    public static final int TRUE = 1;


    protected HashMap<E, GraphNode> vertices;

    public DirectedWeightedGraph() {
        this.vertices = new HashMap<>();
    }


    public void addVertex(E object) {
        if(vertices.containsKey(object)) {
            throw new IllegalArgumentException("Element already exists in this graph. Object: " + object.toString());
        }
        GraphNode node = new GraphNode(object);
        vertices.put(object, node);
    }

    /**
     * Adds the edge to this graph. Provides an option to silently add
     * the objects if they don't already exist on the graph.
     * @param source    the source node.
     * @param target    the target node.
     * @param silentAdd if true, will ensure the source and target are added
     *                  to this graph if they are not already. Otherwise an
     *                  exception will be thrown.
     */
    public GraphEdge addEdge(E source, E target, int weight, boolean silentAdd) {
        return addEdge(source, target, weight, 0, silentAdd);
    }

    private GraphEdge addEdge(E source, E target, int weight, int mark, boolean silentAdd) {
        if(silentAdd) {
            if(!vertices.containsKey(source)) {
                vertices.put(source, new GraphNode(source));
            }
            if(!vertices.containsKey(target)) {
                vertices.put(target, new GraphNode(target));
            }
        } else {
            if(!vertices.containsKey(source)) {
                throw new IllegalArgumentException("Source node not contained in this graph.");
            }
            if(!vertices.containsKey(target)) {
                throw new IllegalArgumentException("Target node not contained in this graph.");
            }
        }
        GraphNode sourceNode = vertices.get(source);
        GraphNode targetNode = vertices.get(target);
        GraphEdge edge = new GraphEdge(targetNode, sourceNode, weight);
        edge.mark = mark;
        sourceNode.edges.add(edge);
        targetNode.indegree++;
        return edge;
    }

    public void addEdge(GraphEdge edge) {
        if(!contains(edge.source.object)) {
            addVertex(edge.source.object);
        }
        if(!contains(edge.target.object)) {
            addVertex(edge.target.object);
        }
        vertices.get(edge.source.object).edges.add(edge);
    }

    public void addBiDirectionalEdge(E v1, E v2, int weight, boolean silentAdd) {
        addBiDirectionalEdge(v1, v2, weight, 0, silentAdd);
    }

    private void addBiDirectionalEdge(E v1, E v2, int weight, int mark, boolean silentAdd) {
        addEdge(v1, v2, weight, mark, silentAdd);
        addEdge(v2, v1, weight, mark, silentAdd);

    }

    /**
     * Removes the vertex, and all associated edges with it.
     */
    public void removeVertex(E object) {
        if(vertices.containsKey(object)) {
            vertices.get(object).edges.forEach(edge -> {
                removeEdge(edge);
            });
            vertices.remove(object);
            vertices.forEach((obj, node) -> {
                node.edges.removeIf(a -> a.target.equals(object));
            });
            
        } else {
            throw new IllegalArgumentException("Object is not an element of this graph.");
        }
    }

    public void removeEdge(GraphEdge edge) {
        GraphNode node = edge.source;
        edge.target.indegree--;
        if(node.edges.remove(edge) == null) {
            throw new IllegalArgumentException("edge is not in graph");
        }
        
    }

    public void removeEdge(E source, E target, int weight) {
        if(vertices.containsKey(source)) {
            GraphNode node = vertices.get(source);
            
            if(!node.edges.removeIf(a -> {
                if(a.target.object.equals(target) && weight == a.weight) {
                    a.target.indegree--;
                    return true;
                } else {
                    return false;
                }
            })) {
                throw new IllegalArgumentException("Edge was not found");
            }
        }
    }

    public boolean contains(E object) {
        return vertices.containsKey(object);
    }

    public boolean containsEdge(E source, E target, int weight) {
        if(vertices.containsKey(source) && vertices.containsKey(target)) {
            GraphNode node = vertices.get(source);
            System.out.println(node);
            return node.edges.contains(a -> 
                a.source.equals(source) && 
                a.target.equals(target) &&
                a.weight == weight);
        } 
        return false;
    }
    
    // public List<E> topSort() {
    //     List<E> list = new ArrayList<>();
    //     DirectedWeightedGraph<E> clone = deepCopy();
    //     Queue<GraphNode> queue = new Queue<>(clone.size());
    //     clone.vertices.forEach((obj, node) -> {
    //         if(node.edges.isEmpty()) {
    //             queue.enqueue(node);
    //         }
    //     });
    //     int i = 0;
    //     while(!queue.isEmpty()) {
    //         GraphNode node = queue.dequeue();
    //         list.add(node.object); 
    //         ListIterator<GraphEdge> iter = node.edges.listIterator();
    //         while(iter.hasNext()) {
    //             GraphEdge e = iter.next();
    //             if(--e.target.indegree == 0) {
    //                 queue.enqueue(e.target);
    //             }
    //             iter.remove();
    //         }
    //         clone.removeVertex(node.object);
    //         i++;
    //     }
    //     if(i != vertices.size()) {
    //         throw new IllegalStateException("Cycle found, cannot perform top sort.");
    //     }
    //     return list;
    // }

    /**
     * Reverses all edges. That is, for any directed edge (uv) that is incident
     * with ordered vertices (u, v), flip it such that the direction of the edge is reversed,
     * i.e. edge (uv) -> (vu). Changes are stored in this graph.
     */
    public DirectedWeightedGraph<E> reverseEdges() {
        resetEdgeMarks((int) Math.pow(2, REVERSE_EDGES_OFS));
        this.vertices.forEach((object, node) -> {
            for(GraphEdge e : node.edges) {
                if(e.getMark(REVERSE_EDGES_SIZE, REVERSE_EDGES_OFS) == 0) {
                    addEdge(e.target.object, e.source.object, e.weight, 1, true);
                }
    
            }
        });
        this.vertices.forEach((object, node) -> {
            node.edges.forEach(e -> {
                if(e.getMark(REVERSE_EDGES_SIZE, REVERSE_EDGES_OFS) == 0) {
                    node.edges.remove(e);
                }
            });
        });
        return this;
    }

    public DirectedWeightedGraph<E> deepCopy() {
        DirectedWeightedGraph<E> result = new DirectedWeightedGraph<>();
        this.vertices.forEach((object, node) -> {
            node.edges.forEach(e -> {
                result.addEdge(object, e.target.object, e.weight, true);
            });
        });
        return result;
    }

    public HashMap<E, GraphNode> getVMap() {
        return vertices;
    }
    public Set<E> getVertices() {
        return vertices.keySet();
    }

    public NodeTraverser bfs() {
        return new NodeTraverser();
    }

    public int size() {
        return vertices.size();
    }
    
    public void resetAllMarks() {
        this.vertices.forEach((object, node) -> {
            node.edges.forEach(e -> {
                e.mark = 0;
            });
            node.mark = 0;
        });
    }

    /**
     * Use -1 for all clear
     */
    public void resetEdgeMarks(int flags) {
        this.vertices.forEach((object, node) -> {
            node.edges.forEach(e -> {
                e.mark &= ~flags;
            });
        });
    }

    /**
     * Use -1 for all clear
     */
    public void resetVertexMarks(int flags) {
        this.vertices.forEach((object, node) -> {
            node.mark &= ~flags;
        });
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(String.format("GRAPH %s%n------%n", super.toString()));
        vertices.forEach((obj, node) -> {
            b.append(String.format("NODE %s | In %6d | Out %6d%n", obj.toString(), node.indegree, node.edges.elementCt()));
            node.edges.forEach(edge -> {
                b.append(String.format("---> %s | Weight %d%n", edge.target.object.toString(), edge.weight));
            });
        });
        return b.toString();
    }

    /**
     * Merges the two graphs into one, saving the changes on this graph.
     * @return this graph
     */
    public DirectedWeightedGraph<E> merge(DirectedWeightedGraph<E> other) {
        other.getVMap().forEach((vert, node) -> {
            node.edges.forEach(edge -> {
                
                addEdge(edge);
            });
        });
        return this;
    }

    /**
     * Finds the connected components of this graph and returns the results
     * in separate graphs.
     * @return
     */
    public List<DirectedWeightedGraph<E>> findConnected() {
        resetVertexMarks(DFS_VISITED_SIZE << DFS_VISITED_OFS);
        //The reversed graph to let us find the parts that are not pointing in
        //the right direction. 
        DirectedWeightedGraph<E> reversed = deepCopy().reverseEdges();
        //Keep track of the vertices we have already used.
        //X -> Y
        Map<E, DirectedWeightedGraph<E>> tracker = new HashMap<>();
        List<DirectedWeightedGraph<E>> results = new ArrayList<>(4);
        Stack<GraphNode> stack = new Stack<>(size());
        vertices.forEach((vert, n) -> {
            DirectedWeightedGraph<E> recent = new DirectedWeightedGraph<>();
            if(tracker.containsKey(n.object)) {
                return;
            }
            stack.push(n);
            while(!stack.isEmpty()) {
                GraphNode node = stack.pop();
                if(!tracker.containsKey(node.object)) {
                    //Found a new part. If the tracker did not contain it
                    //then create a new vertex
                    node.edges.forEach(edge -> {
                        //Add all the node's edges to the graph.
                        if(!recent.containsEdge(edge.source.object, edge.target.object, edge.weight)) {
                            recent.addEdge(edge.source.object, edge.target.object, edge.weight, true);
                        }
                        //Push adjacent nodes if they haven't already been expanded.
                        if(!tracker.containsKey(edge.target.object)) {
                            stack.push(edge.target);
                        }
                    });
                    tracker.put(node.object, recent);
                    node = reversed.vertices.get(node.object);
                    //Check for any in-edges. This will ensure that one-way
                    //components are connected since we can't traverse them
                    //normally; this will allow us to skip merging graphs.
                    node.edges.forEach(edge -> {
                        if(!tracker.containsKey(edge.target.object)) {
                            stack.push(vertices.get(edge.target.object));
                        }
                    });
                }
            }
            results.add(recent);
        });
        return results;
    }

    public abstract class MarkedComponent {

        /**
         * Used as a part of graph algorithms. Call {@link #resetAllMarks}
         * to clear the mark.
         */
        int mark = 0;

        public int mark() {
            return mark;
        }

        public void setMark(int value, int bitCount, int offset) {
            int mask = clearMark(bitCount, offset);
            this.mark |= ((value & (mask)) << offset);
        }

        public int clearMark(int bitCount, int offset) {
            int mask = ((int) Math.pow(2, bitCount) - 1);
            this.mark &= ~(mask << offset);
            return mask;
        }

        public int getMark(int bitCount, int offset) {
            int mask = ((int) Math.pow(2, bitCount) - 1);
            return (this.mark >>> offset) & mask;
        }

    }

    

    public class GraphNode extends MarkedComponent {

        private RBTree<GraphEdge> edges;
        
        private int indegree = 0;
        private E object;
        
        private GraphNode(E object) {
            this.object = object;
            
            this.edges = new RBTree<>();
        }
        
        public int indegree() {
            return indegree;
        }

        public E getObject() {
            return object;
        }

        public RBTree<GraphEdge> edges() {
            return edges;
        }

        public void combine(RBTree<GraphEdge> other) {
            this.edges = edges.union(other);
        }
        

    }

    public class GraphEdge extends MarkedComponent implements Comparable<GraphEdge> {

        private int weight;

        private GraphNode source;
        private GraphNode target;

        private GraphEdge(GraphNode target, GraphNode source, int weight) {
            this.weight = weight;
            this.source = source;
            this.target = target;
        }

        public int hashCode() {
            return Objects.hash(source, target, weight);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean equals(Object other) {
            return 
                other.getClass() == this.getClass() &&
                this.source.equals(((GraphEdge)other).source) &&
                this.target.equals(((GraphEdge)other).target) &&
                this.weight == (((GraphEdge)other).weight);
        }

        @Override
        public int compareTo(GraphEdge other) {
            if(this.weight == other.weight) {
                return 0;
            } else if(this.weight > other.weight) {
                return 1;
            } else {
                return -1;
            }
        }

        public int weight() {
            return weight;
        }

        public GraphNode getSource() {
            return source;
        }

        public GraphNode getTarget() {
            return target;
        }
    }

    /**
     * Acts as an iterator of the graph (similar to iterating through a top-sort
     * of the graph), but works on cycles. Uses breadth-first search.
     */
    public class NodeTraverser {

        private Queue<GraphNode> sources;
        private Queue<GraphNode> bfs;
        private NodeTraverser() {
            this.sources = new Queue<>(vertices.size());
            this.bfs = new Queue<>(vertices.size() * vertices.size() / 2);
            reset();
        }

        /**
         * Returns the next element.
         */
        public E nextTarget() {
            if(bfs.isEmpty()) {
                if(!sources.isEmpty()) {
                    bfs.enqueue(sources.dequeue());
                } else {
                    throw new IllegalStateException("No more graph vertices can be found.");
                }
            }
            
            GraphNode node = bfs.dequeue();
            node.setMark(TRUE, BFS_SIZE, BFS_OFS);
            node.edges.forEach(edge -> {
                if(edge.target.getMark(BFS_SIZE, BFS_OFS) == 0) {
                    bfs.enqueue(edge.target);
                }
            });
            return node.object;
        }

        public void reset() {
            this.bfs.clear();
            this.sources.clear();
            resetVertexMarks((int)Math.pow(2, BFS_OFS));
            vertices.forEach((obj, node) -> {
                if(node.indegree == 0) {
                    sources.enqueue(node);
                }
            });
        }

        public boolean hasNext() {
            return !sources.isEmpty() || !bfs.isEmpty();
        }
    }
}