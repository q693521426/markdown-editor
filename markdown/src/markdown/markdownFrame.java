package markdown;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.markdown4j.Markdown4jProcessor;

public class markdownFrame extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	markdownFrame(){
		super();
		
		JFrame frame=this;
		frame.setTitle("markdown");
	
		GridLayout gridLayout=new GridLayout(1,3,0,10);
		frame.setLayout(gridLayout);
		
		JTree tree=new JTree();
		DefaultMutableTreeNode total=new DefaultMutableTreeNode("total");
		tree.setModel(new DefaultTreeModel(total));
		frame.add(new JScrollPane(tree));
		
		HashMap<String,Integer> map=new HashMap<String,Integer>();
		map.put("total",0);
		
		JTextArea input=new JTextArea();
		frame.add(new JScrollPane(input));
	
		JEditorPane output = new JEditorPane();
		output.setContentType("text/html");  
		output.setEditable(false);
		JScrollPane showPane=new JScrollPane(output);
		frame.add(showPane);
		setCss(output,"");
		
		input.getDocument().addDocumentListener(new DocumentListener(){

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub
				updateText(input,output);
				updateTree(input,map,tree);
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub
				updateText(input,output);
				updateTree(input,map,tree);
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub
				updateText(input,output);
				updateTree(input,map,tree);
			}
		});
	
		tree.addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node=(DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
				if (node == null)
                    return;
				// TODO Auto-generated method stub
				try {
					input.setCaretPosition(input.getLineStartOffset(map.get(node.toString())));
				} catch (BadLocationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		makeMenuBar(frame,input,output);
		
		frame.pack();
		frame.setSize(1024,768);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	private void makeMenuBar(JFrame frame,JTextArea input,JEditorPane output){
		JMenuBar menubar=new JMenuBar();
		frame.setJMenuBar(menubar);
		
		JMenu fileMenu=new JMenu("File");
		menubar.add(fileMenu);
		
		JMenuItem importCss=new JMenuItem("import css");
		importCss.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				openFileDialog(input,output);
			}
		});
		
		JMenuItem save=new JMenuItem("save as docx");
		save.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				saveFileDialog(input,output);
			}
		});
		
		fileMenu.add(importCss);
		fileMenu.add(save);
	}
	
	private void updateText(JTextArea input,JEditorPane output){
		String parse=null;
		try{
			parse = new Markdown4jProcessor().process(input.getText());
			output.setText(parse);
		}catch(IOException e){
			e.printStackTrace();
		}	
	}
	private void addNodeH3(DefaultMutableTreeNode root,DefaultMutableTreeNode node_h1,DefaultMutableTreeNode node_h2,DefaultMutableTreeNode node_h3){
		if(node_h3.getUserObject()==null)return;
		if(node_h1.getUserObject()==null && node_h2.getUserObject()==null)root.add(node_h3);
		else if(node_h1.getUserObject()!=null && node_h2.getUserObject()==null)node_h1.add(node_h3);
		else if(node_h1.getUserObject()==null && node_h2.getUserObject()!=null)node_h2.add(node_h3);
		else if(node_h1.getUserObject()!=null && node_h2.getUserObject()!=null)node_h2.add(node_h3);
	}
	private void addNodeH2(DefaultMutableTreeNode root,DefaultMutableTreeNode node_h1,DefaultMutableTreeNode node_h2){
		if(node_h1.getUserObject()==null && node_h2.getUserObject()!=null)root.add(node_h2);
		else if(node_h2.getUserObject()!=null)node_h1.add(node_h2);
	}
	private void addNodeH1(DefaultMutableTreeNode root,DefaultMutableTreeNode node_h1,DefaultMutableTreeNode node_h2){
		addNodeH2(root,node_h1,node_h2);
		if(node_h1.getUserObject()!=null)root.add(node_h1);
	}
	
	private void updateTree(JTextArea input,HashMap<String,Integer> map,JTree tree){
		Integer[] h={0,0,0};
		
		map.clear();
		map.put("total",0);
		
		String[] s=input.getText().split("\n");
		
 		DefaultMutableTreeNode root=new DefaultMutableTreeNode("total");
		DefaultMutableTreeNode node_h1=new DefaultMutableTreeNode(null),node_h2=new DefaultMutableTreeNode(null),node_h3=new DefaultMutableTreeNode(null);
		for(int i=0;i<s.length;++i){
			if(s[i].trim().startsWith("###")){
				node_h3=new DefaultMutableTreeNode((String)node_h2.getUserObject()+"."+"<h"+h[2].toString()+".3>");
				map.put((String)node_h3.getUserObject(), i);
				++h[2];		
				addNodeH3(root,node_h1,node_h2,node_h3);
			}else if(s[i].trim().startsWith("##")){
				addNodeH2(root,node_h1,node_h2);
				node_h2=new DefaultMutableTreeNode((String)node_h1.getUserObject()+"."+"<h"+h[1].toString()+".2>");
				map.put((String)node_h2.getUserObject(), i);
				++h[1];
				h[2]=0;
				node_h3=new DefaultMutableTreeNode(null);
			}else if(s[i].trim().startsWith("#")){
				addNodeH1(root,node_h1,node_h2);
				node_h1=new DefaultMutableTreeNode("<h"+h[0].toString()+".1>");
				map.put("<h"+h[0].toString()+".1>", i);
				++h[0];
				h[1]=h[2]=0;
				node_h2=new DefaultMutableTreeNode(null);
				node_h3=new DefaultMutableTreeNode(null);
			}	
		}
		addNodeH1(root,node_h1,node_h2);

		tree.setModel(new DefaultTreeModel(root));
		tree.updateUI();
	}
	private void openFileDialog(JTextArea input,JEditorPane output){
		JFileChooser fileChooser=new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);  
		fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f){
                if(f.getName().endsWith("css") || f.isDirectory()) 
                	return true;
                return false;
            }
 
            @Override
            public String getDescription(){
                // TODO Auto-generated method stub
                return "*.css";
            }
        });
		fileChooser.showOpenDialog(null);
		File file=fileChooser.getSelectedFile();  
		if(file==null){
        	System.out.println("未选择文件");
		}else if(file.isDirectory()){  
            System.out.println("文件夹:"+file.getAbsolutePath());
            System.out.println(fileChooser.getSelectedFile().getName());  
        }else if(file.isFile()){  
            System.out.println("文件:"+file.getAbsolutePath());  
            System.out.println(fileChooser.getSelectedFile().getName()); 
            
            setCss(output,file.getAbsolutePath());
      
            updateText(input,output);
 //           input.setText(text);
        }
	}
	private void setCss(JEditorPane output,String path){
		HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet styleSheet = kit.getStyleSheet();
 
        
       	try {
       		if(path==""){
       			styleSheet.importStyleSheet(new URL("file:///"+markdownFrame.class.getClassLoader().getResource("").getFile().toString()+"Default.css"));
       		}else 
       			styleSheet.importStyleSheet(new URL("file:///"+path.replace("\\", "/")));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   
        output.setEditorKit(kit);
	}
	private void saveFileDialog(JTextArea input,JEditorPane output){
		JFileChooser fileChooser=new JFileChooser();
		fileChooser.setSelectedFile(new File("1.doc"));
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);  
		fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f){
                if(f.getName().endsWith("doc") ||f.getName().endsWith("docx") ) 
                	return true;
                return false;
            }
 
            @Override
            public String getDescription(){
                // TODO Auto-generated method stub
                return "*.doc";
            }
        });
		int returnVal =fileChooser.showSaveDialog(null);
		File file = fileChooser.getSelectedFile();
		String fname=fileChooser.getName(file);  
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			  //添加点击打开按钮后的事件
			if(!fname.endsWith("doc") && !fname.endsWith("docx") ) {
				JOptionPane.showMessageDialog(fileChooser, "please save as doc or docx", "alert", JOptionPane.ERROR_MESSAGE); 
				saveFileDialog(input,output);
			}
		} else if(returnVal ==JFileChooser.CANCEL_OPTION) {
			  //添加点击撤销按钮后的事件
			return;
		}
	
		if(fname!=null&&(fname.endsWith(".doc")||fname.endsWith(".docx"))){
			try {
				byte contentByte[] = getFullHTML(output).getBytes();
                ByteArrayInputStream bais = new ByteArrayInputStream(contentByte);
                POIFSFileSystem poifs = new POIFSFileSystem();
                DirectoryEntry directory = poifs.getRoot();
                DocumentEntry documentEntry = directory.createDocument("WordDocument", bais);

                FileOutputStream out = new FileOutputStream(file);
                poifs.writeFilesystem(out);
                poifs.close();
                bais.close();
                out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
     
		}
		
	}
	private String getFullHTML(JEditorPane pane) {
      
        String styleTag = "<style  type=\"text/css\">";
        try {
            FileInputStream in = new FileInputStream(markdownFrame.class.getClassLoader().getResource("").getFile().toString()+"Default.css");
            int size = in.available();
            byte[] buffer = new byte[size];
            in.read(buffer);
            in.close();
            styleTag += new String(buffer, "UTF8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        styleTag += "</style>";

        String content = pane.getText();
        int headTagPos = content.indexOf("<head>");
        content = content.substring(0, headTagPos+6) + styleTag + content.substring(headTagPos+6, content.length());

        return content;
	}
}
