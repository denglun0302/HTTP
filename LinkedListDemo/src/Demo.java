public class Demo {
    public static class Node{
        int val;
        Node next;

        public Node(int val, Node next) {
            this.val = val;
            this.next = next;
        }
    }
    public Node reverse(Node head){
        Node cur=head;
        Node newHead=null;
        while(cur!=null){
            Node next=cur.next;
            cur.next=newHead;
            newHead=cur;
            cur=next;
        }
        return newHead;
    }

}
