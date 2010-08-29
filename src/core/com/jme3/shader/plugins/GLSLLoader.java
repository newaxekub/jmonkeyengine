package com.jme3.shader.plugins;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLoader;
import com.jme3.asset.AssetManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * GLSL File parser that supports #import pre-processor statement
 */
public class GLSLLoader implements AssetLoader {

    private AssetManager owner;
    private Map<String, DependencyNode> dependCache = new HashMap<String, DependencyNode>();

    private class DependencyNode {

        private String shaderSource;
        private String shaderName;

        private final Set<DependencyNode> dependsOn = new HashSet<DependencyNode>();
        private final Set<DependencyNode> dependOnMe = new HashSet<DependencyNode>();

        public DependencyNode(String shaderName){
            this.shaderName = shaderName;
        }

        public void setSource(String source){
            this.shaderSource = source;
        }

        public void addDependency(DependencyNode node){
            if (this.dependsOn.contains(node))
                return; // already contains dependency

//            System.out.println(shaderName + " depend on "+node.shaderName);
            this.dependsOn.add(node);
            node.dependOnMe.add(this);
        }

    }

    private class GlslDependKey extends AssetKey<InputStream> {
        public GlslDependKey(String name){
            super(name);
        }
        @Override
        public boolean shouldCache(){
            return false;
        }
    }

    private DependencyNode loadNode(InputStream in, String nodeName) throws IOException{
        DependencyNode node = new DependencyNode(nodeName);
        if (in == null)
            throw new IOException("Dependency "+nodeName+" cannot be found.");

        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        while (r.ready()){
            String ln = r.readLine();
            if (ln.startsWith("#import ")){
                ln = ln.substring(8).trim();
                if (ln.startsWith("\"") && ln.endsWith("\"") && ln.length() > 3){
                    // import user code
                    // remove quotes to get filename
                    ln = ln.substring(1, ln.length()-1);
                    if (ln.equals(nodeName))
                        throw new IOException("Node depends on itself.");

                    // check cache first
                    DependencyNode dependNode = dependCache.get(ln);
                    if (dependNode == null){
                        GlslDependKey key = new GlslDependKey(ln);
                        // make sure not to register an input stream with
                        // the cache..
                        InputStream stream = (InputStream) owner.loadAsset(key);
                        dependNode = loadNode(stream, ln);
                    }
                    node.addDependency(dependNode);
                }
//            }else if (ln.startsWith("uniform") || ln.startsWith("varying") || ln.startsWith("attribute")){
//                // these variables are included as dependencies as well
//                DependencyNode dependNode = dependCache.get(ln);
//                if (dependNode == null){
//                    // the source and name are the same for variable dependencies
//                    dependNode = new DependencyNode(ln);
//                    dependNode.setSource(ln);
//                    dependCache.put(ln, dependNode);
//                }
//                node.addDependency(dependNode);
            }else{
                sb.append(ln).append('\n');
            }
        }
        r.close();

        node.setSource(sb.toString());
        dependCache.put(nodeName, node);
        return node;
    }

    private DependencyNode nextIndependentNode(List<DependencyNode> checkedNodes){
        Collection<DependencyNode> allNodes = dependCache.values();
        if (allNodes == null || allNodes.size() == 0)
            return null;
        
        for (DependencyNode node : allNodes){
            if (node.dependsOn.size() == 0){
                return node;
            }
        }

        // circular dependency found..
        for (DependencyNode node : allNodes){
            System.out.println(node.shaderName);
        }
        throw new RuntimeException("Circular dependency.");
    }

    private String resolveDependencies(DependencyNode root){
        StringBuilder sb = new StringBuilder();

        List<DependencyNode> checkedNodes = new ArrayList<DependencyNode>();
        checkedNodes.add(root);
        while (true){
            DependencyNode indepnNode = nextIndependentNode(checkedNodes);
            if (indepnNode == null)
                break;

            sb.append(indepnNode.shaderSource).append('\n');
            dependCache.remove(indepnNode.shaderName);
            
            // take out this dependency
            for (Iterator<DependencyNode> iter = indepnNode.dependOnMe.iterator();
                 iter.hasNext();){
                DependencyNode dependNode = iter.next();
                iter.remove();
                dependNode.dependsOn.remove(indepnNode);
            }
        }

//        System.out.println(sb.toString());
//        System.out.println("--------------------------------------------------");
        
        return sb.toString();
    }

    /**
     *
     * @param owner
     * @param in
     * @param extension
     * @param key
     * @return
     * @throws java.io.IOException
     */
    public Object load(AssetInfo info) throws IOException {
        // The input stream provided is for the vertex shader, 
        // to retrieve the fragment shader, use the content manager
        this.owner = info.getManager();
        if (info.getKey().getExtension().equals("glsllib")){
            // NOTE: Loopback, GLSLLIB is loaded by this loader
            // and needs data as InputStream
            return info.openStream();
        }else{
            // GLSLLoader wants result as String for
            // fragment shader
            DependencyNode rootNode = loadNode(info.openStream(), "[main]");
            String code = resolveDependencies(rootNode);
            dependCache.clear();
            return code;
        }
    }

}
