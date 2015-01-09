package edu.buffalo.cse;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class DirectoryManager {
    
    
    public String filepath ;
    public DocumentBuilderFactory docFactory;
    public DocumentBuilder builder;
    public String currentLevel;
    public String currentPath;
    public Document doc;
    public Element currentElement;
    public Node currentNode;
    public String myPort;
    public String currentNodeIp;
    
    public DirectoryManager(InputStream strm, String port)
    {
        {
            //filepath = "src/Local.xml";
            filepath = "res/SourceXML/Local.xml" ;//+
            //		"com/example/smartfs_spring2014/Local.xml";
            
            docFactory = DocumentBuilderFactory.newInstance();
            try {
                builder = docFactory.
                        newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            currentLevel="/root";
            try {
//                doc = builder.parse(filepath);
                doc = builder.parse(strm);
            } catch (SAXException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            currentElement  = doc.getDocumentElement();
            currentNode = doc.getDocumentElement();
            currentPath = "";
            currentNodeIp= "";
            myPort = port;
        }
    }

    
    public ObjectItem[] getCurrentLevelList() throws XPathExpressionException
    {
        System.out.println("Current path:"+ currentPath);
        
        XPathExpression expr = XPathFactory.newInstance().newXPath().compile(currentLevel+ "/NodeElement") ;
        Object hits = expr.evaluate(currentElement, XPathConstants.NODESET) ;
        
        List<ObjectItem> elementList = new ArrayList<ObjectItem>();
        
        if ( hits instanceof NodeList ) {
            NodeList list = (NodeList) hits ;
            System.out.println("Hit Count:" + list.getLength());
            for (int i = 0; i < list.getLength(); i++ ) {
                if(list.item(i).getParentNode().equals(currentElement))
                {
                    elementList.add(new ObjectItem(list.item(i).getAttributes().getNamedItem("Name").getNodeValue()
                                    ,i
                                    ,list.item(i).getAttributes().getNamedItem("type").getNodeValue()
                                    ));
                System.out.println("Node"+i+": " + list.item(i).getAttributes().getNamedItem("Name").getNodeValue());
                }
            }
        }
        ObjectItem arr[];
        if (elementList.size()> 0 )
         arr = new ObjectItem[elementList.size()];
        else
          arr = new ObjectItem[]{};
        
        for (int k = 0 ;k < elementList.size(); k ++ ) 
        {
            arr[k] = elementList.get(k);
        }
        return arr;
    }
    
    public String getIP(String folderName) throws XPathExpressionException
    {
        String Ip = null;
        XPathExpression expr = XPathFactory.newInstance().newXPath().compile(currentLevel+"/NodeElement") ;
        Object hits = expr.evaluate(currentElement, XPathConstants.NODESET) ;

        if ( hits instanceof NodeList ) {
            NodeList list = (NodeList) hits ;
            for (int i = 0; i < list.getLength(); i++ ) {
                if(list.item(i).getAttributes().getNamedItem("Name").getNodeValue().equals(folderName))
                {
                   // Log.e("Message","Folder: "+folderName+" Ip:"+list.item(i).getAttributes().getNamedItem("ip").getNodeValue());
                    return list.item(i).getAttributes().getNamedItem("ip").getNodeValue();
                    
//                    currentElement =(Element)list.item(i);
//                    currentLevel = currentLevel + "/NodeElement";
//                    currentPath = currentPath +"/"+folderName;
//                    return true;
                }
            }
        }
        
        return null;
    }
    
    public boolean gotoNextLevel(String folderName) throws XPathExpressionException
    {
        XPathExpression expr = XPathFactory.newInstance().newXPath().compile(currentLevel+"/NodeElement") ;
        Object hits = expr.evaluate(currentElement, XPathConstants.NODESET) ;

        if ( hits instanceof NodeList ) {
            NodeList list = (NodeList) hits ;
            for (int i = 0; i < list.getLength(); i++ ) {
                if(list.item(i).getParentNode().equals(currentElement) && list.item(i).getAttributes().getNamedItem("Name").getNodeValue().equals(folderName))
                {
                    currentElement =(Element)list.item(i);
                    currentLevel = currentLevel + "/NodeElement";
                    currentPath = currentPath +"/"+folderName;
                    return true;
                }
            }
        }
        
        return false;
    }


    public boolean gotoPreviousLevel()
    {
        if(currentLevel.lastIndexOf('/') == -1 ||  currentPath.lastIndexOf('/') == -1 )
        {
         return false;   
        }
        currentLevel = currentLevel.substring(0, currentLevel.lastIndexOf('/'));
        currentPath = currentPath.substring(0, currentPath.lastIndexOf('/'));
        currentElement = (Element)currentElement.getParentNode();
        return true;
    }
    
    public boolean updateFolder(Node n,String folderName) throws XPathExpressionException
    {
        Log.e("Directory manager","Inside updateFolderFunction");
        XPathExpression expr = XPathFactory.newInstance().newXPath().compile(currentLevel+ "/NodeElement") ;
        Object hits = expr.evaluate(currentElement, XPathConstants.NODESET) ;
        
        if ( hits instanceof NodeList ) {
            NodeList list = (NodeList) hits ;
            System.out.println("Hit Count:" + list.getLength());
            for (int i = 0; i < list.getLength(); i++ ) {
                if(list.item(i).getParentNode().equals(currentElement) && list.item(i).getAttributes().getNamedItem("Name").getNodeValue().equals(folderName))
                {
                    System.out.println("replacing node" + folderName);
                    currentNodeIp =  n.getAttributes().getNamedItem("ip").getNodeValue();
                    currentElement.replaceChild(doc.importNode(n, true),list.item(i));
                 //   Log.e("Message",currentElement.toString());
                    //this.getCurrentLevelList();
                    Log.e("Directory manager","Replaced the node " + folderName+ "with given node");
                    return true;
                }

            }
        }

        return false;
    }

    
}
