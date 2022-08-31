import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.Test;

import com.google.common.io.Files;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;

public class GitHubRepoDownload {
	
	/*This class is created by Etor.
	 * This class is for code similarity check for soft forking / etc */

ArrayList<File[]> fileList = new ArrayList<File[]>();

/**
 * @param githubRemoteUrl Remote git http url which ends with .git.
 * @param accessToken     Personal access token.
 * @param branchName Name of the branch which should be downloaded
 * @param destinationDir  Destination directory where the downloaded files should be present.
 * @return
 * @throws Exception
 */
public boolean downloadRepoContent(String githubRemoteUrl, String destinationDir) throws Exception {
    //String githubSourceUrl, String accessToken
    //CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(accessToken, "");
    URL fileUrl = new URL("file://"+destinationDir);
    File destinationFile = FileUtils.toFile(fileUrl);
    //delete any existing file
    FileUtils.deleteDirectory(destinationFile);
    /*Git.cloneRepository().setURI(githubRemoteUrl)
            .setBranch(branchName)
            .setDirectory(destinationFile)
            .setCredentialsProvider(credentialsProvider)
            .call();*/
    Git.cloneRepository()
    .setURI(githubRemoteUrl)
    .setDirectory(destinationFile)
    .call();
    
    if(destinationFile.length() > 0){
        return true;
    }else{
        return false;
    }
  }

/*function name: copyFile
 * this method is to copy files which needed to compare
 * this method is one of the methods to find soft forking (softForking) repo
 * 
 * Here is steps
 * 1) download two repo
 * 2) copy all source files into one folder for AC2
 * 3) call AC2 for code similarity via AC2 UI
 * 4) check whether repo is forking from original/main repo or not
 * 
 * motivating example: https://github.com/simple-uploader/Uploader/issues/91
 * Unethical behavior: “soft” fork (fork but without adding the git history, and not in the list of forks), rebranding fork
 * */	
	public void copyFile(String from, String to, String prefix,ArrayList<String> filesExtensionToCompare) throws IOException
	{ 		
		
		/*FilenameFilter filter = new FilenameFilter() {
	        @Override
	        public boolean accept(File f, String name) {
	            return name.endsWith(fileExtension);
	        }
	    };*/
		//delete .git file
		File file = new File(from+".git");
		deleteDir(file);
		
		file = new File(from+".github");
		deleteDir(file);
		
		file = new File(from);
		listOfFiles(file,filesExtensionToCompare,prefix,to);
	    //listOfFiles(from,to,file,fileExtension,prefix);
	}
	
	   public static void listOfFiles(File dirPath, ArrayList<String> filesExtensionToCompare, String prefix, String to) throws IOException{
		      File filesList[] = dirPath.listFiles();
		      for(File file : filesList) {
		         if(file.isFile()) {
		        	for(String s: filesExtensionToCompare)
		            {
		        		if(file.getName().endsWith(s)){
		        			//System.out.println(file.getAbsolutePath());
		        		Path src = Paths.get(file.getAbsolutePath());
		 		           Path dest = Paths.get(to+prefix+"_"+file.getName()); 		    		
		 		           Files.copy(src.toFile(), dest.toFile()); 
		        		}
		            }
		         } else {
		            listOfFiles(file,filesExtensionToCompare,prefix,to);
		         }
		      }
		   }
	
	   public static void listOfFiles(String from, String to, File dirPath,String extension,String prefix) throws IOException{
		      File filesList[] = dirPath.listFiles();
		      System.out.println(filesList.length);
		      for(File file : filesList) {
		    	 // System.out.println(file.getAbsolutePath());
		    	 if(file.getName().contains(".git") || file.getName().contains(".github")) continue;
		    	 System.out.println(file.getAbsolutePath());
		         if(file.isFile() && file.getName().endsWith(extension)) {
		            //System.out.println("File path: "+file.getName());
		           Path src = Paths.get(file.getAbsolutePath());
		           Path dest = Paths.get(to+prefix+"_"+file.getName()); 		    		
		           Files.copy(src.toFile(), dest.toFile()); 
		         } else{
		            listOfFiles(from,to,dirPath,extension,prefix);
		         }
		      }
		   }
	   
	   void deleteDir(File file) {
		   try {
		    File[] contents = file.listFiles();
		    if (contents != null) {
		        for (File f : contents) {
		            deleteDir(f);
		        }
		    }
		    file.delete();
		   }catch(Exception e) {
			   
		   }
		}
}
