import java.util.ArrayList;
import java.util.List;

public class Dictionary {
    private static class TrieNode {
        TrieNode[] children = new TrieNode[26];
        boolean isWord;
        String definition;
        int subtreeCount;

        int childCount() {
            int count = 0;
            for (TrieNode n : children) {
                if (n != null && n.subtreeCount > 0) {
                    count++;
                }
            }
            return count;
        }

        int getOnlyChildIndex() {
            for (int i = 0; i < 26; i++) {
                TrieNode n = children[i];
                if (n != null && n.subtreeCount > 0) {
                    return i;
                }
            }
            return -1;
        }
    }

    private static class CompressedNode {
        String segment;
        boolean isWord;
        String definition;
        int subtreeCount;
        List<CompressedNode> children = new ArrayList<>();

        CompressedNode(String segment, boolean isWord, String definition, int subtreeCount) {
            this.segment = segment;
            this.isWord = isWord;
            this.definition = definition;
            this.subtreeCount = subtreeCount;
        }
    }

    private TrieNode root;
    private CompressedNode cRoot;
    private boolean compressed;

    /**
     * Constructor to initialize the Dictionary
     */
    public Dictionary() {
        this.root = new TrieNode();
        this.compressed = false;
    }

    /**
     * A method to add a new word to the dictionary
     * If the word is already in the dictionary, this method will change its
     * definition
     *
     * @param word       The word we want to add to our dictionary
     * @param definition The definition we want to associate with the word
     */
    public void add(String word, String definition) {
        if (compressed) {
            return;
        }
        TrieNode curr = root;
        List<TrieNode> path = new ArrayList<>();
        path.add(curr);
        for (char ch : word.toCharArray()) {
            int idx = ch - 'a';
            if (curr.children[idx] == null) {
                curr.children[idx] = new TrieNode();
            }
            curr = curr.children[idx];
            path.add(curr);
        }
        if (!curr.isWord) {
            for (TrieNode node : path) node.subtreeCount++;
        }
        curr.isWord = true;
        curr.definition = definition;
    }

    /**
     * A method to remove a word from the dictionary
     *
     * @param word The word we want to remove from our dictionary
     */
    public void remove(String word) {
        if (compressed) {
            return;
        }
        TrieNode curr = root;
        List<TrieNode> path = new ArrayList<>();
        path.add(curr);
        for (char ch : word.toCharArray()) {
            int idx = ch - 'a';
            if (curr.children[idx] == null) {
                return;
            }
            curr = curr.children[idx];
            path.add(curr);
        }
        if (!curr.isWord) {
            return;
        }
        curr.isWord = false;
        curr.definition = null;
        for (TrieNode node : path) {
            node.subtreeCount--;
        }
    }

    /**
     * A method to get the definition associated with a word from the dictionary
     * Returns null if the word is not in the dictionary
     *
     * @param word The word we want to get the definition for
     * @return The definition of the word, or null if not found
     */
    public String getDefinition(String word) {
        if (!compressed) {
            TrieNode curr = root;
            for (char ch : word.toCharArray()) {
                int idx = ch - 'a';
                if (curr.children[idx] == null) return null;
                curr = curr.children[idx];
            }
            if(curr.isWord) {
                return curr.definition;
            }
            return null;
        }
        CompressedNode curr = cRoot;
        int pos = 0;
        while (pos < word.length()) {
            boolean found = false;
            for (CompressedNode child : curr.children) {
                String seg = child.segment;
                if (word.startsWith(seg, pos)) {
                    pos += seg.length();
                    curr = child;
                    found = true;
                    break;
                }
            }
            if (!found) {
                return null;
            }
        }

        if(curr.isWord) {
            return curr.definition;
        }
        return null;
    }

    /**
     * Gives the number of words in the dictionary with the given prefix
     *
     * @param prefix The prefix we want to count words for
     * @return The number of words that start with the prefix
     */
    public int countPrefix(String prefix) {
        if (!compressed) {
            TrieNode curr = root;
            for (char ch : prefix.toCharArray()) {
                int idx = ch - 'a';
                if (curr.children[idx] == null) return 0;
                curr = curr.children[idx];
            }
            return curr.subtreeCount;
        }
        CompressedNode curr = cRoot;
        int pos = 0;
        if (prefix.isEmpty()) {
            return curr.subtreeCount;
        }
        while (pos < prefix.length()) {
            boolean found = false;
            String rem = prefix.substring(pos);
            for (CompressedNode child : curr.children) {
                String seg = child.segment;
                if (rem.startsWith(seg)) {
                    pos += seg.length();
                    curr = child;
                    found = true;
                    break;
                } else if (seg.startsWith(rem)) {
                    return child.subtreeCount;
                }
            }
            if (!found) {
                return 0;
            }
        }
        return curr.subtreeCount;
    }

    /**
     * Compresses the trie by combining nodes with single children
     * This operation should not change the behavior of any other methods
     */
    public void compress() {
        if (compressed) {
            return;
        }
        this.cRoot = new CompressedNode("", false, null, root.subtreeCount);
        for (int i = 0; i < 26; i++) {
            if (root.children[i] != null) {
                char ch = (char) ('a' + i);
                cRoot.children.add(compressEdge(root.children[i], String.valueOf(ch)));
            }
        }
        this.root = null;
        this.compressed = true;
    }

    /**
     * A method to get a string representation of the sequence of nodes which would
     * store the word
     * in a compressed trie consisting of all words in the dictionary
     * Returns null if the word is not in the dictionary
     *
     * @param word The word we want the sequence for
     * @return The sequence representation, or null if word not found
     */
    public String getSequence(String word) {
        if (!compressed) {
            return null;
        }
        List<String> parts = new ArrayList<>();
        CompressedNode curr = cRoot;
        int pos = 0;
        while (pos < word.length()) {
            boolean found = false;
            for (CompressedNode child : curr.children) {
                String seg = child.segment;
                if (word.startsWith(seg, pos)) {
                    parts.add(seg);
                    pos += seg.length();
                    curr = child;
                    found = true;
                    break;
                }
            }
            if (!found) {
                return null;
            }
        }
        return String.join("-", parts);
    }

    private CompressedNode compressEdge(TrieNode node, String prefix) {
        TrieNode curr = node;
        StringBuilder sb = new StringBuilder(prefix);
        while (curr.childCount() == 1) {
            int idx = curr.getOnlyChildIndex();
            sb.append((char) ('a' + idx));
            curr = curr.children[idx];
        }
        CompressedNode cnode = new CompressedNode(sb.toString(), curr.isWord, curr.definition, curr.subtreeCount);
        for (int i = 0; i < 26; i++) {
            if (curr.children[i] != null) {
                char ch = (char) ('a' + i);
                cnode.children.add(compressEdge(curr.children[i], String.valueOf(ch)));
            }
        }
        return cnode;
    }
}
