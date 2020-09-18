package firenoo.lib.structs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Red-black tree implementation. This tree is not synchronized.
 * The add and remove operations uses a top-down implementation.
 * The join-based operations, e.g. join, are taken from this
 * paper.
 * <a href="https://arxiv.org/abs/1602.02120">
 *  "Parallel Ordered Sets Using Join." Guy B., et. al. 12 Nov. 2016.
 *  DOI: 10.1145/2935764.2935768
 * </a>.
 * <p>Elements: </p>
 * <ul>
 *  <li>Duplicates are allowed and are first stored on the left branch.
 *      Rotations will create the possibility of duplicates found on
 *      the right branch, however.</li>
 *  <li> Null values are not allowed.</li>
 * </ul>
 * @param <E> the type to hold, must be {@link java.lang.Comparable}. It is 
 *            not necessary for the natural ordering to be consistent with 
 *            equals; both {@code a.compareTo(b) == 0} and 
 *            {@code a.equals(b)} are both used to check for equivalency.
 *            
 */
public class RBTree<E extends Comparable<E>> implements Iterable<E> {
    
    public RBNode root;
    private int elementCt;
    private int maxHeight = 1;

    public RBTree() {
        root = null;
        elementCt = 0;
    }


    /**
     * <h4>Add</h4>
     * Adds the element to this tree, with the red-black guarantee that the
     * tree's height will not exceed 2 * log2(n).
     * <h4>Implementation Details</h4>
     * Null elements are not permitted. Duplicate elements are permitted and
     * are prioritized to be be placed on the left side first. Rotations can
     * create situations where duplicate elements are stored on the right
     * as well as the left.
     * @param object the object to store.
     * @throws IllegalArgumentException if object is null.
     */
    public void add(E object) {
        if(object == null) 
            throw new IllegalArgumentException("Null values" +
            "are not allowed!");
        this.maxHeight = (int) (2 * 
            Math.log1p(++elementCt) / Math.log(2));
        if(root == null) {
            root = new RBNode(object, null, true);
            return;
        }

        RBNode curNode = root;
        boolean left = false;
        while(curNode != null) {
            left = object.compareTo(curNode.object) <= 0;
            if(curNode.black 
            && !isBlack(curNode.left) 
            && !isBlack(curNode.right)) {
                curNode.black = false;
                curNode.left.black = true;
                curNode.right.black = true;
                if(!isBlack(curNode.parent)) fixRedRed(curNode, left);
            }

            if(left) { 
                if(curNode.left != null) {
                    curNode = curNode.left;
                } else {
                    curNode.left = new RBNode(object, curNode, false);
                    curNode = curNode.left;
                    break;
                }
            } else {
                if(curNode.right != null) {
                    curNode = curNode.right;
                } else {
                    curNode.right = new RBNode(object, curNode, false);
                    curNode = curNode.right;
                    break;
                }
            }            
        }   
        //Fix root
        root.black = true;
        //The only time this is used is if adding to a red node.
        if(!isBlack(curNode.parent)) fixRedRed(curNode, left);
        refresh_bh();
    }

    public boolean addIfNotPresent(E object) {
        if(!contains(object)) {
            add(object);
            return true;
        }
        return false;
    }
    
    public boolean contains(E object) {
        if(object == null) return false;
        RBNode curNode = root;
        while(curNode != null) {
            if(object.equals(curNode.object) 
            || object.compareTo(curNode.object) == 0) {
                return true;
            }
            if(object.compareTo(curNode.object) > 0) {
                curNode = curNode.right;
            } else {
                curNode = curNode.left;
            }
        }
        return false;    
    }

    public boolean contains(Predicate<E> condition) {
        return stream().anyMatch(condition);
    }

    /**
     * Returns the first object found in the tree that is equal to the 
     * supplied object, via {@code Object.equals(E)}. Uses binary search.
     * @param object the object to retrieve
     * @return the object in the tree, or null if it is not present.
     */
    public E get(E object) {
        RBNode node = getNode(object);
        return node == null ? null : node.object;    

    }

    /**
     * Returns all the objects found in this tree.
     * @param object
     * @return
     */
    @SuppressWarnings("unchecked")
    public E[] getAll(E object) {
        Object[] query = getCount(object);
        Comparable<E>[] result = new Comparable[(Integer) query[0]];
        RBNode node = (RBNode) query[1];
        Stack<RBNode> stack = new Stack<>(elementCt);
        stack.push(node);
        while(!stack.isEmpty()) {
            node = stack.pop();
            if(node.right != null 
            && (node.right.object.equals(object) 
            || node.right.object.compareTo(object) == 0)) {
                stack.push(node.right);
            }
            if(node.left != null 
            && (node.left.object.equals(object) 
            || node.left.object.compareTo(object) == 0)) {
                stack.push(node.left);
            }
        }
        return (E[]) result;


    }

    private RBNode getNode(E object) {
        RBNode curNode = root;
        while(curNode != null 
        && (!curNode.object.equals(object) 
        || curNode.object.compareTo(object) != 0)) {
            if(object.compareTo(curNode.object) > 0) {
                curNode = curNode.right;
            } else {
                curNode = curNode.left;
            }
        }
        return curNode;
    }

    /**
     * Gets the element with the highest natural ordering.
     * @return if there are duplicates with the highest natural ordering,
     *         the exact element returned is not definitive.
     */
    public E getHighest() {
        RBNode iter = root;
        if(iter == null) return null;
        while(iter.right != null) {
            iter = iter.right;
        }
        return iter.object;
    }

    /**
     * Gets the element with the lowest natural ordering.
     * @return if there are duplicates with the lowest natural ordering,
     *         the exact element returned is not definitive.

     */
    public E getLowest() {
        RBNode iter = root;
        if(iter == null) return null;
        while(iter.left != null) {
            iter = iter.left;
        }
        return iter.object;
    }

    /**
     * Removes the given element from the tree. Note that this method removes
     * only one element at a time for trees that allow duplicate elements.
     * @param object the object to remove. The object is compared using
     *               {@code a.compareTo(b) == 0} and {@code a.equals(b)}.
     * @return the object that was removed.
     */
    public E remove(E object) {
        if(!contains(object)) {
            return null;
        }
        E result = null;
        this.maxHeight = (int) (2 * Math.log1p(--elementCt / Math.log(2)));
        RBNode p = root, x, t;
        boolean left = false;
        boolean old_left = left;
        boolean justFound = false;
        while(p != null) {
            left = !justFound ? object.compareTo(p.object) <= 0 : left;
            //Ensure that the node to be deleted will be red.
            //Step 1: Transform P to red, and X & T to black.
            if(left) {
                //Left subproblem
                x = p.left;
                t = p.right;
            } else {
                //Right subproblem
                x = p.right;
                t = p.left;
            }

            if(!justFound && object.equals(p.object)){
                //We found it. Now we delete it
                if(x == null && t == null) {
                    //Case 1: Both null children.
                    if(p.parent != null) {
                        if(old_left) {
                            p.parent.left = null;
                        } else {
                            p.parent.right = null;
                        }
                        root.black = true;
                    } else {
                        root = null;
                    }
                    refresh_bh();
                    return result == null ? p.object : result;
                } else if(p.right == null) {
                    //Case 2: Right child is null.
                    //Traverse down the left side to find the largest left node
                    RBNode iter = p.left;
                    while(iter.right != null) {
                        iter = iter.right;
                    }
                    //save the deleted on first find
                    if(result == null) result = p.object;
                    //Swap the values of p with the smallest right leaf. 
                    //Then delete the actual leaf
                    p.object = iter.object;
                    object = iter.object;
                    // p = p.left;
                    // old_left = true;
                    left = true;
                    justFound = true;
                } else {
                    //Case 2: Either Left child is null or there are two 
                    //nonnull children.
                    //Traverse down the right side to find the smallest 
                    //right node
                    RBNode iter = p.right;
                    while(iter.left != null) {
                        iter = iter.left;
                    }
                    //save the deleted on first find
                    if(result == null) result = p.object;
                    //Swap the values of p with the smallest right leaf. Then 
                    //delete the actual leaf
                    p.object = iter.object;
                    object = iter.object;
                    // p = p.right;
                    // old_left = false;
                    left = false;
                    justFound = true;
                }
                //Now delete the next instance of the replacer object (which,
                //if there is a dupe, should keep percolating down)
                continue; 
            }
            //Transform p to red, x and t are black.
            if(isBlack(p)) {
                if(isBlack(x) && isBlack(t)) {
                    //Case 1: Both x and t are black.
                    //Solution: Mark p as red.
                    p.black = false;
                } else if(left && isBlack(x)) {
                    //Case 2.1: One of the children is red, and the next node
                    //is black (left side)
                    //Solution: Rotate left and recolor.
                    rotateL(t, p, p.parent, old_left);
                    p.black = false;
                    t.black = true;
                    t = p.right; //update t
                } else if(isBlack(x)) {
                    //Case 2.2: One of the children is red, and the next node
                    //is black (right side)
                    //Solution: Rotate right and recolor.
                    rotateR(t, p, p.parent, old_left);
                    p.black = false;
                    t.black = true;
                    t = p.left; //update t
                } else {
                    //Case 2.2: One of the children is red, and the next node
                    //is red
                    //Solution: Fall through to next level.
                    p = x;
                    old_left = left;
                    justFound = false;
                    continue;
                }
            }
            //Now that p is guaranteed to be red
            if(isBlack(x.left) && isBlack(x.right)) {
                //Case 1: Both children of x are black
                if(isBlack(t.left) && isBlack(t.right)) {
                    //Case 1.1: Both children of t are black.
                    //Color flip on p, x, t.
                    p.black = true;
                    t.black = false;
                    x.black = false;    
                } else if(isBlack(t.right)){
                    //Case 1.2: left child of t is red
                    if(left) {
                        //Right -> left
                        rotateR(t.left, t, p, false);
                        rotateL(t.parent, p, p.parent, old_left);
                        p.black = true;
                        x.black = false;
                    } else {
                        //Left -> left
                        rotateR(t, p, p.parent, old_left);
                        t.black = false;
                        p.black = true;
                        t.left.black = true;
                        x.black = false;
                    }
                } else {
                    //Case 1.3: right child of t is red
                    if(!left) {
                        //Left -> right
                        rotateL(t.right, t, p, !left);
                        rotateR(t.parent, p, p.parent, old_left);
                        p.black = true;
                        x.black = false;
                    } else {
                        //Right -> right
                        rotateL(t, p, p.parent, old_left);
                        t.black = false;
                        p.black = true;
                        t.right.black = true;
                        x.black = false;
                    }
                }
                
            
            }
            //Case 2. One of x's children are red.
            //Fall through to the next level.
            
            //Fall through to the next subproblem
            p = x;
            justFound = false;
            old_left = left;
        }
        refresh_bh();
        return null;
    }

    /**
     * Removes all the objects that are equal to the supplied object, and
     * returns them in an array.
     */
    @SuppressWarnings("unchecked")
    public E[] removeAll(E object) {
        int toRemove = (Integer) getCount(object)[0];
        Object[] results = new Object[toRemove];
        for(int i = 0; i < toRemove; i++) {
            results[i] = remove(object);
        }
        return(E[]) results;
    }

    /**
     * Gets the number of elements that are equal or has the same natural
     * order as the supplied object, as well as the first node encountered.
     * @return 0 - int, number of elements
     *         1 - node, the first node encountered (should be equal to 
     *             {@link #get(Comparable)}) 
     */
    private Object[] getCount(E object) {
        RBNode node = getNode(object);
        Stack<RBNode> tracker = new Stack<>(elementCt);
        Object[] result = new Object[] {0, node};
        if(node == null) return result;
        tracker.push(node);
        int i = 1;
        while(!tracker.isEmpty()) {
            node = tracker.pop();
            if(node.right != null 
            && (node.right.object.equals(object) 
            || node.right.object.compareTo(object) == 0)) {
                tracker.push(node.right);
            }
            if(node.left != null 
            && (node.left.object.equals(object) 
            || node.left.object.compareTo(object) == 0)) {
                tracker.push(node.left);
            }
            i++;
        }
        result[0] = i;
        return result;    
    }

    /**
     * <h4>Union</h4>
     * Merges all nodes in {@code other} with the nodes in this tree.
     * Neither tree is modified; the returned tree is independent of
     * either (i.e. {@code other} is first deep copied.)<br></br>
     * <h4>Implementation Behavior</h4>
     * ****THIS IS NOT EQUIVALENT TO THE MATHEMATICAL SET DEFINITION 
     * OF UNION**** <br></br>
     * Note that nodes in both trees are added regardless of whether they
     * test true for {@link Object#equals(Object)}. That is, even if
     * {@code a.equals(b)}, a and b will both be present in the resultant
     * tree. The same is true for {@code a.compareTo(b) == 0}.
     * @param other the other tree. 
     */
    public RBTree<E> union(RBTree<E> other) {
        RBTree<E> o1 = deepCopy();
        RBTree<E> o2 = other.deepCopy();
        if(o1.root == null) {
            return o2;
        } else if(o2.root == null) {
            return o1;
        } else {
            RBTree<E> result = new RBTree<E>();
            //Ensure there is enough capacity
            result.elementCt = o1.elementCt + o2.elementCt;
            result.maxHeight = 
                (int) (2 * Math.log1p(result.elementCt / Math.log(2)));
            //Real union happens here
            result.root = result.union(o1.root, o2.root);
            //Fix black root violation
            if(!isBlack(result.root)) result.root.black = true;
            //refresh properties
            result.updateElemCt(result.elementCt);
            result.refresh_bh();
            return result;
        }
    }

    /**
     * Node version of {@link #union(RBTree)}. This method WILL modify
     * both nodes and their associated trees.
     */
    @SuppressWarnings("unchecked")
    private RBNode union(RBNode node1, RBNode node2) {
        if(node1 == null) {
            return node2;
        } else if(node2 == null) {
            return node1;
        } else {
            Object[] s = split(node1, node2.object,true);
            RBNode left = union((RBNode) s[0], node2.left);
            RBNode right = union((RBNode) s[2], node2.right);
            refresh_bh(left, elementCt);
            refresh_bh(right, elementCt);
            return join(left, node2.object, right, null, false);
        }
    }

    /**
     * <h4>Intersect</h4>
     * Returns a tree that contains all the elements that are contained in
     * both this tree and the supplied tree. An element {@code a} is 
     * considered to be in both trees iff {@code a} is in this tree and there
     * exists some element {@code b} in the other tree, such that 
     * {@code a.equals(b)} or {@code a.compareTo(b) == 0}. <br></br>
     * Because this is a set operation, for trees that allow duplicates, this
     * operation will not work as intended.
     * <h4>Implementation Behavior</h4>
     * For the following discussion, let {@code A} be this tree, {@code B} be
     * the supplied tree, and {@code C} be the result tree.
     * <ol>
     *  <li>
     *      Results: <br></br>
     *      The elements chosen to be put in the final result is currently
     *      undefined (have not looked in-depth at the moment).
     *  </li>
     *  <li>
     *      Dupes: <br></br>
     *      An element {@code x} is defined to be a duplicate of element 
     *      {@code y} iff {@code x.equals(y)} or {@code x.compareTo(y) == 0}.
     *      The number of duplicates in {@code C} is equal to the number of
     *      duplicates in {@code A} or {@code B}, whichever has fewer 
     *      duplicates. The actual duplicate elements chosen are undefined.
     *  </li>
     *  
     * </ol>
     * No elements are modified in any of the source trees. This method calls
     * {@link #deepCopy()} in both sources prior to any modification.
     * @param other the other tree. 
     * @return a new tree
     */
    public RBTree<E> intersect(RBTree<E> other) {
        RBTree<E> o1 = this.deepCopy();
        RBTree<E> o2 = other.deepCopy();
        RBTree<E> result = new RBTree<>();
        result.elementCt = Math.max(o1.elementCt, o2.elementCt);
        result.root = result.intersect(o1.root, o2.root);
        result.updateElemCt(result.elementCt);
        result.refresh_bh();
        return result;
    }

    /**
     * Node version of {@link #intersect(RBTree)}. This method WILL modify
     * both nodes and their associated trees.
     * @return a new tree that contains all the elements that are present in
     *         both source trees.
     */
    @SuppressWarnings("unchecked")
    private RBNode intersect(RBNode node1, RBNode node2) {
        if(node1 == null || node2 == null) return null;
        Object[] t = split(node1, node2.object, true);
        RBNode left  = intersect((RBNode) t[0], node2.left);
        RBNode right = intersect((RBNode) t[2], node2.right);
        refresh_bh(left, elementCt);
        refresh_bh(right, elementCt);
        if((Boolean) t[1]) {
            return join(left, node2.object, right, null, false);
        } else {
            return join2(left, right);
        }
    }

    /**
     * <h4>Difference</h4>
     * Performs set difference. To be more precise, let {@code A} be this tree,
     * {@code B} be the supplied tree, and {@code C} be the returned tree; 
     * {@code C = A - B}. Any element in {@code C} is present in {@code A} but
     * not in {@code B}.
     * <h4>Implementation Details</h4>
     *  <ol>
     *  <li>
     *      Results: <br></br>
     *      The elements chosen to be put in the final result is always from
     *      this tree (the caller).
     *  </li>
     *  <li>
     *      Dupes: <br></br>
     *      An element {@code x} is defined to be a duplicate of element 
     *      {@code y} iff {@code x.equals(y)} or {@code x.compareTo(y) == 0}.
     *      The number of duplicates in {@code C} is equal to the number of
     *      duplicates in {@code A} - the number of duplicates in {@code B}.
     *      The actual duplicate elements chosen are undefined.
     *  </li>
     *  
     * </ol>
     * No elements are modified in any of the source trees. This method calls
     * {@link #deepCopy()} in both sources prior to any modification.
     * @param other the other tree.
     * @return a tree that is the result of performing set difference on the 
     *         source trees.
     */
    public RBTree<E> difference(RBTree<E> other) {

    
        RBTree<E> o1 = this.deepCopy();
        RBTree<E> o2 = other.deepCopy();
        RBTree<E> result = new RBTree<>();
        result.elementCt = Math.max(o1.elementCt, o2.elementCt);
        result.root = result.difference(o1.root, o2.root);
        result.updateElemCt(result.elementCt);
        result.refresh_bh();
        return result;

        
    }

    /**
     * Node version of {@link #difference(RBTree)}. This method WILL modify
     * both trees.
     */
    @SuppressWarnings("unchecked")
    private RBNode difference(RBNode node1, RBNode node2) {
        if(node1 == null) {
            return null;
        } else if(node2 == null) {
            return node1;
        }
        Object[] t = split(node1, node2.object, false);
        RBNode left = difference((RBNode) t[0], node2.left);
        RBNode right = difference((RBNode) t[2], node2.right);
        refresh_bh(left, elementCt);
        refresh_bh(right, elementCt);
        return join2(left, right);
    }

    /**
     * Splits the tree into two parts. This method uses an iterative approach
     * as opposed to the recursive algorithm described in the reference paper.
     * @param node the topmost node to split.
     * @param key  the object to split the tree on. In the result, any element
     *             in the tree that is less than the key 
     *             (via {@code Comparable.compareTo(E)}) is in found in the 
     *             left split, while any element in the tree that is greater
     *             is found in the right split.
     * @return An object array containing:
     * <ol>
     *     <li>RBNode  - left side of the split</li>
     *     <li>boolean - whether {@code key} was found in the tree.</li>
     *     <li>RBNode  - right side of the split</li>
     * </ol>
     */
    private Object[] split(RBNode node, E key, boolean allowDupes) {
        //     Strategy: 
        // (1) Start at X. 
        // (2) Traverse the tree. We add the left node and right node.
        // (3) Then if we reach a leaf or find T, we retrace our path 
        //     back to X, joining left and right as we go.
        Object[] result = new Object[3];
        RBNode iter = node, left = null, right = null, parent = iter.parent;
        Stack<Boolean> path = new Stack<>(maxHeight);
        boolean containsKey = false;
        //Going down - Probing
        while(iter != null) {
            if(key.equals(iter.object) || key.compareTo(iter.object) == 0) {
                containsKey = true;
                if(allowDupes) {
                    left = join(iter.left, iter.object, null, null, false);
                } else {
                    left = iter.left;
                }
                right = iter.right;
                break;
            }
            parent = iter;
            if(key.compareTo(iter.object) < 0) {
                //Move to the left
                iter = iter.left;
                path.push(true);
            } else {
                //Move to the right
                iter = iter.right;
                path.push(false);
            }
        }

        while(!path.isEmpty()) {
            boolean p_left = path.pop();
            if(p_left) {
                right = join(right, 
                             parent.object, 
                             parent.right, 
                             null, 
                             path.peek());
            } else {
                left = join(parent.left, 
                            parent.object, 
                            left, 
                            null, 
                            path.peek());
            }
            parent = parent.parent;
        }
        refresh_bh(left, elementCt);
        refresh_bh(right, elementCt);
        result[0] = left;
        result[1] = containsKey;
        result[2] = right;
        return result;
    }

    /**
     * Joins the two nodes. Scans the right branch of the first tree
     * and joins the trees together using the largest element found.
     * @param left left node.
     * @param right right node.
     */
    @SuppressWarnings("unchecked")
    private RBNode join2(RBNode left, RBNode right) {
        if(left == null) return right;
        Object[] objs = splitLast(left);
        return join((RBNode) objs[0], (E) objs[1], right, null, false);
    }

    /**
     * Splits the last node to the right of k. Employs the same strategy as
     * {@link #split(RBNode, Comparable)}, but instead of looking for a
     * particular node, this method simply looks for the node with the greatest
     * natural order.
     * @return An array of objects:
     * <ol> 
     *      <li>RBNode - the nodes left over after splitting </li>
     *      <li>E      - the node object that was removed. </li>
     * </ol>
     */
    @SuppressWarnings("unchecked")
    private Object[] splitLast(RBNode k) {
        Object[] result = new Object[2];
        result[0] = k.left;
        result[1] = k.object;
        RBNode iter = k.left;
        RBNode parent = k;
        while(iter != null) {
            iter = iter.right;
            result[1] = parent.object;
        }

        while(parent != k) {
            parent = parent.parent;
            result[0] = join((RBNode) result[0], 
                             parent.object, 
                             parent.left, 
                             null, 
                             false);
        }
        return result;
    }

    /**
     * Join right procecdure. Attaches to the right node of parent.
     * Note that left < key < right for any node in left and right.
     * Symmetrical to joinLeft.
     * @return the topmost node affected.
     */
    private RBNode joinRight(RBNode left, E key, RBNode right) {
        //Iteratative:
        left.parent = null;
        RBNode iter = left;
        RBNode parent = iter.parent;
        while(iter != null && bh(iter) != bh(right)) {
            refresh_bh(iter, elementCt);
            parent = iter;
            iter = iter.right;
        }
        RBNode k = new RBNode(key, parent, false);
        if(parent != null) parent.right = k;
        k.left = iter;
        k.right = right;
        if(iter != null) iter.parent = k;
        if(right != null) right.parent = k;
        iter = k;
        RBNode p_0 = iter.parent;
        do {
            RBNode p_1 = p_0 != null ? p_0.parent : null;
            if(p_0 != null && p_0.black 
            && !iter.black && !isBlack(iter.right)) {
                rotateL(iter, p_0, p_1, false);
                iter.left.black = true;
                iter.right.black = true;
                p_0 = iter.parent;
            } else if(!isBlack(p_0)){
                iter = iter.parent;
                p_0 = iter.parent;
            } else {
                break;
            }
        } while(p_0 != null);
        while(iter != null && iter.parent != null) {
            iter = iter.parent;
        }
        return iter;
    }

    /**
     * Join left procecdure. Attaches to the left node of parent.
     * Note that left < key < right for any node in left and right.
     * Symmetrical to joinRight.
     */
    private RBNode joinLeft(RBNode left, E key, RBNode right) {
        //Iteratative:
        right.parent = null;
        RBNode iter = right;
        RBNode parent = iter.parent;
        while(iter != null && bh(iter) != bh(left)) {
            refresh_bh(iter, elementCt);
            parent = iter;
            iter = iter.left;
        }
        RBNode k = new RBNode(key, parent, false);
        if(parent != null) parent.left = k;
        k.left = left;
        k.right = iter;
        if(iter != null) iter.parent = k;
        if(left != null) left.parent = k;
        iter = k;
        RBNode p_0 = iter.parent;
        do {
            RBNode p_1 = p_0 != null ? p_0.parent : null;
            if(p_0 != null && p_0.black && !iter.black && !isBlack(iter.left)) {
                rotateR(iter, p_0, p_1, false);
                iter.left.black = true;
                iter.right.black = true;
                p_0 = iter.parent;
            } else if(!isBlack(p_0)){
                iter = iter.parent;
                p_0 = iter.parent;
            } else {
                break;
            }
        } while(p_0 != null);
        while(iter != null && iter.parent != null) {
            iter = iter.parent;
        }
        return iter;    
    }

    /**
     * Merges left, right and key together.
     * @param left   the left node
     * @param key    key. left < key < right for each node in left and right.
     * @param right  right node
     * @param parent node to attach to. {@code null} if root.
     * @param p_left whether to attach to left or right of parent. 
     *               Ignored if parent is {@code null}, or when 
     *               {@code rank(left) = rank(right)}.
     * @return the topmost node after the join operation finishes
     */
    private RBNode join(RBNode left, E key, RBNode right, RBNode parent, boolean p_left) {
        if(bh(left) > bh(right)) {
            //Case 1: left rank is greater than right rank
            RBNode t = joinRight(left, key, right);
            if(parent != null) parent.right = t;
            t.parent = parent;
            if(parent == null && !isBlack(t) && !isBlack(t.right)) {
                t.black = true;
            }
            refresh_bh(t, elementCt);
            return t;
        } else if(bh(left) < bh(right)) {
            RBNode t = joinLeft(left, key, right);
            if(parent != null) parent.left = t;
            t.parent = parent;
            if(!t.black && !isBlack(t.left)) {
                t.black = true;
            }
            refresh_bh(t, elementCt);
            return t;
        } else if(isBlack(left) && isBlack(right)) {
            RBNode n = new RBNode(key, parent, false);
            if(left != null) left.parent = n;
            n.left= left;
            if(right != null) right.parent = n;
            n.right = right;
            refresh_bh(n, elementCt);
            if(p_left && parent != null) {
                parent.left = n;
            } else if(parent != null) {
                parent.right = n;
            }
            return n;
        } else {
            RBNode n = new RBNode(key, parent, true);
            if(left != null) left.parent = n;
            n.left= left;
            if(right != null) right.parent = n;
            n.right = right;
            refresh_bh(n, elementCt);
            if(p_left) {
                parent.left = n;
            } else if(parent != null) {
                parent.right = n;
            }
            return n;
        }
    }

    /**
     * Helper method for performing a null test and returning the black height
     * @return -1 if node is {@code null}, otherwise the black height of the
     *         node.
     */
    private int bh(RBNode node) {
        if(node == null) return -1;
        return node.bh;
    }

    private void refresh_bh() {
        refresh_bh(root, elementCt);
    }

    /**
     * Updates the black height of all nodes under {@code node}, inclusive. 
     * (very lazy) Call this after any modification to the tree.
     * The thread should have a write lock prior to calling this method.
     * This also requires optimization....
     */
    private void refresh_bh(RBNode node, int stackSize) {
        if(node == null) return;
        int height = 0;
        RBNode iter = node;
        Stack<RBNode> process = new Stack<>(stackSize);
        if(iter.right != null) process.push(iter.right);
        if(iter.left != null) process.push(iter.left);
        while(iter != null) {
            if(iter.black) height++;
            if(iter.left != null) {
                iter = iter.left;
            } else if(iter.right != null){
                iter = iter.right;
            } else {
                break;
            }
        }
        node.bh = height;
        while(!process.isEmpty()) {
            iter = process.pop();
            if(iter.parent != null) {
                iter.bh = iter.parent.bh - (iter.parent.black ? 1 : 0);
            }
            if(iter.right != null) {
                process.push(iter.right);
            }
            if(iter.left != null) {
                process.push(iter.left);
            }
        }
    }

    public int elementCt() {
        return elementCt;
    }

    /**
     * Right rotation, no recoloring is done. The thread should have a write
     * lock prior to calling this method.
     * @param x        Becomes the new root.
     * @param p_0      parent of x (NONNULL)
     * @param p_1      parent of p_0
     * @param p_1_left whether p_0 is p_1's left child. Ignored if p_1 is null
     */
    private void rotateR(RBNode x, RBNode p_0, RBNode p_1, boolean p_1_left) {
        p_0.left = x.right;
        if(p_0.left != null) p_0.left.parent = p_0;
        x.right = p_0;
        p_0.parent = x; 
        x.parent = p_1;
        if(p_1 != null) {
            if(p_1_left) {
                p_1.left = x;
            } else {
                p_1.right = x;
            }
        } else {
            root = x;
        }

    }

    /**
     * Left rotation, no recoloring is done. The thread should have a write
     * lock prior to calling this method.
     * @param x        Becomes the new root.
     * @param p_0      parent of x (NONNULL)
     * @param p_1      parent of p_0. If null root is set to x.
     * @param p_1_left whether p_0 is p_1's left child. Ignored if p_1 is null
     */
    private void rotateL(RBNode x, RBNode p_0, RBNode p_1, boolean p_1_left) {
        p_0.right = x.left;
        if(p_0.right != null) p_0.right.parent = p_0;
        x.left = p_0;
        p_0.parent = x;
        x.parent = p_1;
        if(p_1 != null) {
            if(p_1_left) {
                p_1.left = x;
            } else {
                p_1.right = x;
            }
        } else {
            root = x;
        }
 
    }

    /**
     * Fix red-red violation. Shorthand for the other one
     */
    private void fixRedRed(RBNode x, boolean p_0_left) {
        boolean p_1_left = x.parent.parent != null 
                        && x.parent.parent.left == x.parent;
        boolean p_2_left = x.parent.parent != null 
                        && x.parent.parent.parent != null 
                        && x.parent.parent.parent.left == x.parent.parent;
        fixRedRed(x, x.parent, x.parent.parent, p_0_left, p_1_left, p_2_left);
        
    }

    /**
     * Fix red-red violation.
     * @param x        the current node (red)
     * @param p_0      parent of x (red)
     * @param p_1      parent of p_0 (black)
     * @param p_0_left whether x is p_0's left child
     * @param p_1_left whether p_0 is p_1's left child. Ignored if p_1 is null
     * @param p_2_left whether p_1 is its parent's left child. 
     *                 Ignored if p_1 is null.
     */
    private void fixRedRed(RBNode x, RBNode p_0, RBNode p_1, boolean p_0_left,
                           boolean p_1_left, boolean p_2_left) {
        if(p_1_left) {
            //Left branch
            if(p_0_left && !isBlack(x)) {
                //If x is red and on the left side of p_0
                rotateR(p_0, p_1, p_1.parent, p_2_left);
                p_0.black = true;
                p_1.black = false;
            } else if(!isBlack(x)) {
                //If x is red and on the right side of p_0
                rotateL(x, p_0, p_1, p_1_left);
                rotateR(x, p_1, p_1.parent, p_2_left);
                x.black = true;
                p_0.black = false;
                p_1.black = false;
            }
        } else {
            //Right branch
            if(!p_0_left && !isBlack(x)) {
                //If x is red and on the right side of p_0
                rotateL(p_0, p_1, p_1.parent, p_2_left);
                p_0.black = true;
                p_1.black = false;
            } else if(!isBlack(x)) {
                //If x is red and on the right side of p_0
                rotateR(x, p_0, p_1, p_1_left);
                rotateL(x, p_1, p_1.parent, p_2_left);
                x.black = true;
                p_0.black = false;
                p_1.black = false;
            }
        }
    }

    private boolean isBlack(RBNode node) {
        return node == null || node.black;
    }

    /**
     * Copies the node, attaching it to the parent.
     * @param node the node to be copied
     * @param parent the parent of {@code node}
     * @param left whether to attach on the parent's left or right side.
     * @return a shallow copy of node.
     */
    private RBNode copyOf(RBNode node, RBNode parent, boolean left) {
        RBNode n = new RBNode(node.object, parent, node.black);
        if(left && parent != null) {
            parent.left = n;
        } else if(parent != null) {
            parent.right = n;
        }
        n.bh = node.bh;
        return n;
    }

    /**
     * Update the element count and max height count.
     * @param maxElems used for the stack size
     */
    private void updateElemCt(int maxElems) {
        Stack<RBNode> stack = new Stack<>(maxElems);
        int i = 0;
        if(root != null) stack.push(root);
        RBNode iter;
        while(!stack.isEmpty()) {
            i++;
            iter = stack.pop();
            if(iter.left != null) {
                stack.push(iter.left);
            }
            if(iter.right != null) {
                stack.push(iter.right);
            }
        }
        this.maxHeight = (int) (2 * Math.log1p(i) / Math.log(2));
        this.elementCt = i;    
    }

    /**
     * Creates a "deep" copy of the nodes in this tree. It is a deep copy
     * in the sense that the nodes are not linked with the ones in this tree,
     * but the object references used are the same.
     */
    public RBTree<E> deepCopy() {
        RBNode iter = root;
        RBNode node = copyOf(iter, null, false);
        RBNode newRoot = node;
        while(iter != null) {
            if(iter.left != null && node.left == null) {
                iter = iter.left;
                node.left = copyOf(iter, node, true);
                node = node.left;
            } else if(iter.right != null && node.right == null) {
                iter = iter.right;
                node.right = copyOf(iter, node, false);
                node = node.right;
            } else {
                node = node.parent;
                iter = iter.parent;
            }
        }
        RBTree<E> tree = new RBTree<>();
        tree.root = newRoot;
        tree.elementCt = this.elementCt;
        tree.maxHeight = this.maxHeight;
        return tree;    
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString()).append('\n');
        builder.append(root);
        return builder.toString();    

    }

    /**
     * Returns a stream of elements in this tree. The elements are sorted
     * by their natural ordering.
     */
    public Stream<E> stream() {
        if(root == null) return Stream.empty();
        return toList(root).stream();
    }

    /**
     * Returns an array with elements sorted by their natural ordering.
     */
    public Object[] toArray() {
        return toArray(root);
    }

    public List<E> asList() {
        return toList(root);
    }

    private List<E> toList(RBNode rt) {
        if(root == null) return Collections.emptyList();
        List<E> list = new ArrayList<>(elementCt);
        Stack<RBNode> stack = new Stack<>(elementCt);
        RBNode node = rt;
        //in-order traversal
        while(!stack.isEmpty() || node != null) {
            if(node != null) {
                stack.push(node);
                node = node.left;
            } else {
                node = stack.pop();
                list.add(node.object);
                node = node.right;
            }
        }
        return list;
    }

    private Object[] toArray(RBNode rt) {
        return toList(rt).toArray();
    }
    
    /**
     * Changes made to the objects should not affect their natural ordering,
     * which could cause problems.
     * @param action
     */
    public void forEach(Consumer<? super E> action) {
        if(root == null) return;
        toList(root).forEach(action);
    }

    /**
     * Removes the element if it satisfies the condition.
     * @return true iff an element was removed.
     */
    public boolean removeIf(Predicate<E> condition) {
        boolean removed = stream().anyMatch(condition);
        stream().filter(condition).forEach(a -> remove(a));
        return removed;
    }

    /**
     * Returns the iterator. Removing elements via the iterator will not remove
     * elements from this tree!
     */
    public Iterator<E> iterator() {
        if(elementCt == 0) return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return false;
            }
            @Override
            public E next() {
                throw new NoSuchElementException();
            }
        };
        return this.toList(root).iterator();
    }

    private class RBNode {

        private E object;
        private boolean black;
        private int bh = 0;
        private RBNode left, right, parent;

        private RBNode(E object, RBNode parent, boolean black) {
            this.object = object;
            this.black = black;
            this.parent = parent;
            if(black) bh = 1;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            if(black) {
                builder.append("B").append(bh).append(":");
            } else {
                builder.append("R").append(bh).append(":");
            }
            builder.append(object.toString()).append('\n');
            builder.append("Left: \n").append(left).append('\n');
            builder.append("Right: \n").append(right);
            return builder.toString();
        }

    }
}