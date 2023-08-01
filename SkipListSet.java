import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.*;

public class SkipListSet  <T extends Comparable<T>> implements SortedSet<T>{


    private class SkipListSetItem {
        SkipListSetItem up;
        SkipListSetItem down;
        SkipListSetItem left;
        SkipListSetItem right;
        T payload;
        
        SkipListSetItem(){
           
        }

        public SkipListSetItem (T pay) {
            up = null;
            down =null;
            left = null;
            right = null;

            payload = pay;
        }

        public int compareTo(T other){

            //returns int decribing the difference between the payload and the other item
            return this.payload.compareTo(other);
        }

    }

    private int heightOfSkipList = 0;

    //holds top left node and top right node of the list
    private SkipListSetItem heads;
    private SkipListSetItem rear;

    //holds bottom left node and bottom right node of the list
    private SkipListSetItem bottomHeads;
    private SkipListSetItem bottomRear;

    //amount of elements in list
    private int size;
     
     public SkipListSet(){

        //creates new empty skiplist
        SkipListSetItem below = new SkipListSetItem(null);
        SkipListSetItem above = new SkipListSetItem(null);

        below.right = above;
        above.left = below;
        heads = below;
        rear = above;
        bottomHeads = heads;
        bottomRear = rear;
        size = 0;
    }

    public SkipListSet(Collection <? extends T> c){

        //creates a new skiplist and adds all the items into it
        SkipListSetItem below = new SkipListSetItem(null);
        SkipListSetItem above = new SkipListSetItem(null);

        below.right = above;
        above.left = below;
        heads = below;
        rear = above;
        bottomHeads = heads;
        bottomRear = rear;
        size = 0;

        addAll(c);
    }
     
    private class SkipListSetIterator<T extends Comparable<T>> implements Iterator<T>{
        SkipListSetItem iter = bottomHeads;

        public boolean hasNext() {

            //returns wheather the next item is null or not
            return (next() != null) ? true : false;
        }

        public T next() {
            //returns payload and goes to next node
            T item = (T) iter.right.payload;
            iter = iter.right;

            return item;
        }

        public void remove() {
            //removes individual items from the list
            SkipListSetItem rev = iter.right;
                removeLinks(rev);
                
                while(iter != null){
                    removeLinks(rev);
                    //size --;
                    if(rev.up != null){
                        rev = rev.up;
                    }
                    else{
                        break;
                    }
                }
                size --;
        }
        
    }

    public void reBalance(){

        //adds all items in new skiplist with different heights
        SkipListSet<T> reB = new SkipListSet<T>();

        while(bottomHeads.right.payload != null){
            reB.add(bottomHeads.right.payload);

            bottomHeads = bottomHeads.right;
        }

        //replace original skip list with new one
        heads= reB.heads;
        rear = reB.rear;
        heightOfSkipList = reB.heightOfSkipList;
        size = reB.size();
        bottomHeads = reB.bottomHeads;
        bottomRear = reB.bottomRear;

        
    }
    private int getSize(){
        //gets size
        return this.size;
    }

    public int size() {
        //gets size
        size = getSize();
        //returns it
        return size;
       
    }

    public boolean isEmpty() {
        if(bottomHeads.right.payload == null)
        {
            return true;
        }

        return false;
    }

    public boolean contains (Object o) {
        T key = (T) o;

        //returns false if list is empty
        if(isEmpty()){
            return false;
        }

        //gets potential node and determine is it matches or not
         SkipListSetItem n = getPosition(key);


       if(n != null && n.payload != null && n.compareTo(key) == 0){
           
            return true;
        }

        return false;
    }

    public SkipListSetItem getPosition(Object o) {
        T key = (T)o;

        int distance = heightOfSkipList;
        SkipListSetItem n = heads;

        //traverse down and right for every item less than the key
        while(n.down!=null){
            n = n.down;
            distance --;

            while( n.right.payload != null && (n.right.compareTo(key) < 0 || n.right.compareTo(key)==0)){
                n = n.right;
            }
        }

        //return whichever node it stops at
        return n;
    }

    public Iterator<T> iterator() {
        //returns skiplistsetiterator
        Iterator<T> i = new SkipListSetIterator<T>();

        return i;
    }

    public Object[] toArray() {

        //creates object array and stores all elements in it
        Object[] array = new Object[size];
        int i = 0;

        SkipListSetItem bob = bottomHeads;

        while(bob.right.payload != null){
            array[i]= bob.right.payload;
            i++;
            bob = bob.right;
        }

        //returns array
        return array;

    }


    public <T> T[] toArray(T[] a) {

        //returns an array of type T
        //if the length of the array less than length of skiplist change it
        if(a.length < size){
            a = Arrays.copyOf(a, size);
        }
        SkipListSetItem r = bottomHeads;

        for(int i = 0; i < size; i++) {
           if(r.right.payload != null){
                a[i] = (T) r.right.payload;
                r = r.right;
            }
        }

        return a;

    }

    public boolean add(T e) {

        //adding fails if item is already present
        if(contains(e) == true){
            return false;
        }

        //gets the position where the item should go
        SkipListSetItem spot = getPosition(e);

        SkipListSetItem q;

        int level = -1;
        int numOfHeads = -1;

        Random rand = new Random();

        //while true add a level
        //chances of being false happens after every addition
        do{
            numOfHeads++;
            level++;

            canIncreaseHeight(level);

            q = spot;

            while(spot.up == null){
                spot = spot.left;
            }

            if(spot!= null && spot.up != null){
                spot = spot.up;
            }

            q = addAbove(spot, q, e);

  
        }while(rand.nextBoolean() == true);

        //increase size
        size++;
        return true;
     
    }

    private SkipListSetItem addAbove(SkipListSetItem position, SkipListSetItem q, T e){

        //creates new node and handles reference assignments
        SkipListSetItem newItem = new SkipListSetItem(e);
        SkipListSetItem nodeBefore = position.down.down;

        SetLeftRight(q, newItem);
        setUpDown( position, e, newItem, nodeBefore);

        return newItem;

    }

    private void SetLeftRight(SkipListSetItem q, SkipListSetItem newItem){

        //assigns left and right refernces of new node
        newItem.right = q.right;
        newItem.left = q;
        q.right.left = newItem;
        q.right = newItem;
    }

    private void setUpDown(SkipListSetItem position, T e, SkipListSetItem newItem, SkipListSetItem nodeBefore){

        //assigns the above and below references of new node
        if(nodeBefore != null){
            while(true){
                if(nodeBefore.right != null && nodeBefore.right.compareTo(e) != 0){
                    nodeBefore = nodeBefore.right;
                }
                else{
                    break;
                }
            }

             newItem.down = nodeBefore.right;

             nodeBefore.right.up = newItem;
        }

        if(position != null){
            if(position.right.payload != null && position.right.compareTo(e)== 0){
                newItem.up = position.right;
            }
        }
    }
    private void addLevel(){

        //creates bounds for new level
        SkipListSetItem newHead = new SkipListSetItem();
        SkipListSetItem newRear = new SkipListSetItem();

        newHead.right = newRear;
        newHead.down = heads;
        newRear.left = newHead;
        newRear.down = rear;
        
        heads.up = newHead;
        rear.up = newRear;

        heads =newHead;
        rear = newRear;
    }

    private void canIncreaseHeight(int level){
       
        //checks to see a new level can be added
        if(level >= heightOfSkipList){
            heightOfSkipList++;
            addLevel();
        }

    }

    public void printSkipList(){
        //temporary printing
        StringBuilder sb = new StringBuilder();

        sb.append("\nStarting at top left node\n");

        SkipListSetItem t = heads;

        SkipListSetItem topT = t;
        int level = heightOfSkipList;
        while(topT != null){
            sb.append("\n Level: "+level+"\n");
                while(t != null){
                    sb.append(t.payload);

                    if(t.right != null){
                        sb.append(" : ");
                    }

                    t = t.right;
                }

                topT = topT.down;
                t = topT;
                level --;

            }
        System.out.println(sb.toString());
    }

    public int getHeight(){
        //returns max heightOfSkipList;
        return heightOfSkipList;
    }

    public boolean remove(Object o) {
        T key = (T)o;
       
        //if list is empty or item is not in list return false;
        if(isEmpty()){
            return false;
        }

        if(!contains(key)){
            return false;
        }

        //if true get position and delete from every level
        SkipListSetItem delNode = getPosition(key);

        removeLinks(delNode);
        
        while(delNode != null){
            removeLinks(delNode);

            if(delNode.up != null){
                delNode = delNode.up;
            }
            else{
                break;
            }
        }
        //decrease size by one
        size --;

        return true;
    }

    private void removeLinks(SkipListSetItem delNode){

        //detaches and reassign references
        SkipListSetItem afterDelNode = delNode.right;
        SkipListSetItem beforeDelNode = delNode.left;

        beforeDelNode.right = afterDelNode;
        afterDelNode.left = beforeDelNode;
    }
   
    public boolean containsAll(Collection<?> c) {
        
        //returns true if all items are in the skip list
        for(Object o : c){
            if(contains(o) == false){
                return false;
        }
    }

        return true;
    }

    public boolean addAll(Collection<? extends T> c) {

        //returns true if the list changes via add
        boolean sucess = false;
        for(T con : c){
            if(!contains(con)){
                add(con);
                sucess = true;
            }
        }

        return sucess;
    }

    public boolean retainAll(Collection<?> c) {

        // returns true if new skiplist is able to add(retain) some if not all in collection
         SkipListSet<T> set = new SkipListSet<T>();
        
         boolean completed = false;
        for(Object o : c){
            if(contains(o)){
                completed = true;
                T key = (T) o;
                set.add(key);
            }
        }

        //if able to retain replace old skiplist with new skiplist
        if(completed == true){
            heads= set.heads;
            rear = set.rear;
            heightOfSkipList = set.heightOfSkipList;
            size = set.size();
            bottomHeads = set.bottomHeads;
            bottomRear = set.bottomRear;
        }
        return completed;
    }

    public boolean removeAll(Collection<?> c) {

        // returns true if the skip list changes via remove
        boolean ret = false;
        if(isEmpty()){
            return false;
        }
        for(Object o : c){
            if(contains(o)){
                remove(o);
                ret = true;
            }        
        }

        return ret;
    }

  
    public void clear() {

    //resets the skip list and clears all the elements
        heads.right = rear;
        rear.left = heads;
        heads.down = null;
        rear.down = null;
        heightOfSkipList = 0;
        size = 0;
        bottomHeads = heads;
        bottomRear = rear;

    }

    public Comparator<? super T> comparator() {
       return null;
    }

    public SortedSet<T> subSet(T fromElement, T toElement) {
 
        throw new UnsupportedOperationException("Unimplemented method 'subSet'");
    }

    public SortedSet<T> headSet(T toElement) {

        throw new UnsupportedOperationException("Unimplemented method 'headSet'");
    }

    public SortedSet<T> tailSet(T fromElement) {
        throw new UnsupportedOperationException("Unimplemented method 'tailSet'");
    }

    public T first() {
        //returns the first element
            return bottomHeads.right.payload;
    }

    public T last() {

        //returns the last element
            return bottomRear.left.payload;
    }

    public boolean equals(Object obj) {
        if(!(obj instanceof Set)){return false;}

        //if(this.hashCode() != obj.hashCode()){
            //return false;
        //}

            Object [] tempList = this.toArray();

            Set<T> newSet = (Set<T>)obj;

            Object [] tempSet = newSet.toArray();

            if(size != newSet.size()) {return false;}

            Arrays.sort(tempSet);

            for(int i = 0; i < size; i++) {
                if(tempList[i] != tempSet[i]) {return false;}
            }

        return true;
    }

    public int hashCode(){
        int fin = 0;

        for(T e : this){
            fin +=e.hashCode();
        }

       // SkipListSetItem pass = bottomHeads;

        //while(pass.right.payload != null){
            //fin +=pass.right.payload.hashCode();

            //pass = pass.right;
        //}

        return fin;
    }

}
