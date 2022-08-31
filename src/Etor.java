import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHIssueEvent;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;
import org.kohsuke.github.GHCommit.File;
import org.kohsuke.github.GHRepository.Contributor;

import wiremock.org.apache.commons.io.FilenameUtils;

public class Etor {
	
/*This class is created by Etor.*/
	
	static HashMap<String,String> specialLicenseList;
	static ArrayList<String> filesExtensionToCompare;
	static String token="";	
	
	/*function name: setUp
	 *this method is to connect the github without repo name*/
	
	public static GitHub setUp() throws IOException {
		 GitHub gitHub = GitHub.connectUsingOAuth(token);
		 return gitHub;
	}

	/*function name: setUp(String PRRepo)
	 *this method is to connect the github with repo name*/
	
	public static GHRepository setUp(String PRRepo) throws IOException {
		GitHub gitHub = GitHub.connectUsingOAuth(token);
		return gitHub.getRepository(PRRepo);
	}
	
	/* function name: readFromWebPage
	 * this method is to read the word from web page and check whether it includes the keyword (i.e., purchase) that looked for
	 * this method is one of the functions to find the unmaintained project with paid service
   */
	public static boolean readFromWebPage(String urlStr) {
		BufferedReader br = null;
        try {
            URL url = new URL(urlStr);  
            URLConnection connection=url.openConnection();
            connection.setConnectTimeout(1000);
            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String sCurrentLine = br.lines().collect(Collectors.joining());
            if(sCurrentLine.toUpperCase().contains("in-app purchas".toUpperCase())) {
            	return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                System.out.println("*** IOException for URL : ");
            }
        }
        return false;
	}
	
	/* function name: UnmaintainedAndroidWithPaidService(String repo)
	 * this method is to find whether the project latest release date is more than one year
	 * this method is to find whether the readme file includes GooglePlay link
	 * 
	 * Here is step to find the unmaintained project with paid service
	 * 1) check what is the latest date of project maintainence.
	 * 2) check readme for GooglePlay link
	 * 3) check playstore app is in-app purchase
	 */
	
	public static void UnmaintainedAndroidWithPaidService(String repo) throws IOException {
		System.out.println("UnmaintainedAndroidWithPaidService");
		GHRepository repository = setUp(repo);
		if(repository.isFork()) {
			System.out.println("!original : " + repo );
			return;
		}
		boolean readMeOk=true;
		boolean playStore = false;
		boolean paidTrue = false;
		boolean oneYearAgo=false;
		try {
			readMeOk = repository. getReadme()!=null && repository. getReadme().isFile() && repository. getReadme().read()!=null;
		}catch(Exception e) {
			readMeOk=false;
			//System.out.println("error while checking for read me"+repository.getHtmlUrl());
		}
		if(readMeOk) {
		InputStream  is = repository. getReadme().read();
		BufferedReader reader = null;
		//boolean found= false;
		try{
		reader = new BufferedReader(new InputStreamReader(is));
		String str=reader.lines().collect(Collectors.joining());
		str = concatLink(str,"play.google.com");
		
        if(str!=null && str.length()>0) {
        	playStore=true;
        		if(readFromWebPage(str)) {
        			paidTrue = true;
        		}
        	}
        
        Date d = repository.getLatestRelease()!=null?repository.getLatestRelease().getPublished_at():null;
        
        Date date = new Date(System.currentTimeMillis());
        if(d.before(date)) {
        	oneYearAgo=true;
        }        
        
        System.out.println(" PlayStore: "+playStore+", paidTrue: "+paidTrue + ", Repo: "+repo+ ", "+str+", "+d  +" , oneYearAgo="+oneYearAgo);
        }catch(Exception e) {
        	e.printStackTrace();
            	//System.out.println("error while reading web page : "+repository.getHtmlUrl());
         }finally {reader.close();}
	}
		//PagedIterable<GHRelease>
	}
	
		
	/* function name: concatNoAttribution
	 * this method is to concat web link (i.e., stackoverflow weblink) in issue/pr 
	 * this method is one of noAttributionToTheAuthorInCode
	 * 
	 * Here is steps to find the issue/ pull request which doesn't give the credit to the original stackoverflow link in the source code
	 * 1) check  whether issue/pull request include stackoverflow link
	 * 2) if there is stackoverflow link, check whether owners of code in stackoverflow is same with github reporter.
	 * 3) if not same, check whether code commited includes the stackoverflow link or not 
	 */
	public static String concatLink(String st, String keyword) {	    
	      List<String> list = new ArrayList<>();
	      String regexString = "\\b(https://|www[.])[A-Za-z0-9+&@#/%?=~_|!:,.;]*[-A-Za-z0-9+&@#/%=~_|]";
	      Pattern pattern = Pattern.compile(regexString,Pattern.CASE_INSENSITIVE);
	      Matcher matcher = pattern.matcher(st);
	      while (matcher.find()) {
	    	  list.add(st.substring(matcher.start(0),matcher.end(0)));
	      }
      for(String str:list)
	    if(str.contains(keyword)) return str;
      
      return"";
		
	}
	
	/* function name: readFromWebPageForNoAttribution
	 * this method is to read the word from web page and check whether it includes the keyword (i.e., userID) that looked for
	 * this method is one of noAttributionToTheAuthorInCode
	 * 
	 * Here is steps to find the issue/ pull request which doesn't give the credit to the original stackover link in the souce code
	 * 1) check  whether issue/pull request include stackoverflow link
	 * 2) if there is stackoverflow link, check whether owners of code in stackoverflow is same with github reporter.
	 * 3) if not same, check whether code commited includes the stackoverflow link or not
	 */
	public static boolean readFromWebPageForNoAttribution(String urlStr,String userID) {
		BufferedReader br = null;
		boolean found=false;
		boolean diffID = false;
        try {
        	URL url = new URL(urlStr);  
            br = new BufferedReader(new InputStreamReader(url.openStream()));

            String foundWord = userID;          
            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                if (sCurrentLine.contains(foundWord)) {
                	found=true;
                	break;
                }
            }
            if(!found) {
            	diffID = true;
            }
            
            
            //System.out.println(githublink+" , "+urlStr +" , foundName =" + found +" , differentUserID =" + diffID);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
              
            }
        }
        return diffID;
	}
	
		
	
	/*function name: noAttributionCheckInCode
	 * this method is to find the keyword (i.e., stackoverflow) in issue
	 * this method is one of noAttributionToTheAuthorInCode
	 * 
	 * Here is steps to find the issue/ pull request which doesn't give the credit to the original stackover link in the souce code
	 * 1) check  whether issue/pull request include stackoverflow link
	 * 2) if there is stackoverflow link, check whether owners of code in stackoverflow is same with github reporter.
	 * 3) if not same, check whether code commited includes the stackoverflow link or not 
	 * */
	
	public static void noAttributionCheckInCode(String repo,boolean issueType, int n) throws IOException {
		//codeSimilarityUsingAC2();
		boolean found=false;
		boolean credit=false;
		String slink="";
		boolean slinkF=false;
		boolean falsePositive=true; 
		GHRepository r = setUp(repo);
		GHIssue i=null;
		GHPullRequest pr=null;
		String userID="";
		if(issueType) {
			i = r.getIssue(n);
			if(i!=null) {
				try{
					slink = concatLinkTmpHyphen(i.getBody(),"stackoverflow.com");
					if(slink.trim().length()>0) {
						slinkF = true;userID=i.getUser().getLogin();
					}
				}catch(Exception e) {}				
				
				if(!slinkF) {try{
					slink = concatLinkTmpHyphen(i.getTitle(),"stackoverflow.com");
					if(slink.trim().length()>0) {
						slinkF = true;userID=i.getUser().getLogin();
					}
				}catch(Exception e) {}}
				
				if(!slinkF) {try{ 
					List<GHIssueComment> commets=i.getComments();
					 for(GHIssueComment c: commets) {
						 if(c.getBody()!=null) {
							 slink = concatLinkTmpHyphen(c.getBody(),"stackoverflow.com");
							 if(slink.trim().length()>0) {
								 slinkF = true;userID=i.getUser().getLogin();
							 }
						 	}
					 	 }
					}catch(Exception e) {}}					
			}
		}else {
			pr = r.getPullRequest(n);
			if(pr!=null) {
				try{
					slink = concatLinkTmpHyphen(pr.getBody(),"stackoverflow.com");
					if(slink.trim().length()>0) {
						slinkF = true;userID=pr.getUser().getLogin();
					}
				}catch(Exception e) {}				
				
				if(!slinkF) {try{
					slink = concatLinkTmpHyphen(pr.getTitle(),"stackoverflow.com");
					if(slink.trim().length()>0) {
						slinkF = true;userID=pr.getUser().getLogin();
					}
				}catch(Exception e) {}}
				
				if(!slinkF) {try{ 
					List<GHIssueComment> commets=pr.getComments();
					 for(GHIssueComment c: commets) {
						 if(c.getBody()!=null) {
							 slink = concatLinkTmpHyphen(c.getBody(),"stackoverflow.com");
							 if(slink.trim().length()>0) {
								 slinkF = true;userID=pr.getUser().getLogin();
							 }
						 	}
					 	 }
					}catch(Exception e) {}}	
			}
		}
		
		if(slinkF && slink.length()>0){
			found=readFromWebPageForNoAttribution(slink,userID);
		}
		if(slinkF && slink.length()>0 && found) {
			//falsePositive=false;
			if(issueType && i!=null) {
				PagedIterable<GHIssueEvent> events = i.listEvents();
				Iterator<GHIssueEvent> iteratorEvent = events.iterator();
			       while (iteratorEvent.hasNext()) {
			    	   GHIssueEvent e = iteratorEvent.next();
			    	   if(e.getCommitUrl()!=null) {
			    		   //patchURL = e.getCommitUrl();
			    		   String ct=""+e.getCommitId();
			    		   List<GHCommit.File> l = r.getCommit(ct).getFiles();
			    		   for(GHCommit.File f:l) {
			    			   String ps = f.getPatch();
			    			   if(ps!=null && ps.length()>0) {
			    				   falsePositive=false;
			    				   if(ps.toUpperCase().contains("https://stackoverflow.com/".toUpperCase())) {
			    				   //if(ps.toUpperCase().contains(slink.toUpperCase())) {
			    					   credit=true; break;
			    			       }		    				  
			    			   }
			    		   }
				    }
			}}
			
			if(!issueType && pr!=null && slink.length()>0) {
				//falsePositive=false;
				PagedIterable<GHPullRequestFileDetail> tmp = pr.listFiles();
				Iterator<GHPullRequestFileDetail> iterator = tmp.iterator();
			    while (iterator.hasNext()) {
			    	GHPullRequestFileDetail f= iterator.next();
			    	String p = f.getPatch();
			    	if(p!=null && p.length()>0) {
	 				   falsePositive=false;
	 				   if(p.toUpperCase().contains("https://stackoverflow.com/".toUpperCase())) {
	 				   //if(p.toUpperCase().contains(slink.toUpperCase())) {
	 					   credit=true; break;
	 			       }		    				  
	 			   }
			    	//ch = f.getChanges();
			    	//patchURL = f.getContentsUrl().toString();
			    	//GHContent ghc= r.getFileContent(url);
			    }		    	
			  }		
		}
		System.out.println("found: "+found+", repo : "+ repo + ", issue : "+ issueType +", n : "+n +", link: "
		+slink +", credit : "+credit +", falsePositive : "+falsePositive +", userID="+userID);
	}
	
	public static boolean codeSimilarityUsingAC2() throws IOException {
		// Run a java app in a separate system process
		Process proc = Runtime.getRuntime().exec("java -jar lib/ac-2.1.5-SNAPSHOT-UpdatedForEtor.jar");
		// Then retreive the process output
		InputStream in = proc.getInputStream();
		InputStream err = proc.getErrorStream();
		BufferedReader reader = null;
		boolean isDuplicated=false;
		boolean isSimilar=false;
		int duplicatedFileCount=0;
		int similarFileCount=0;
		try {
		reader = new BufferedReader(new InputStreamReader(in));
		String str=""; 
	                   
	        while ((str = reader.readLine()) != null) {
	        	System.out.println(str);
	        	if(str.contains("Detected EXACT duplicate")) {
	        		isDuplicated=true;
	        		duplicatedFileCount=duplicatedFileCount+1;
	        	}
	        	if(str.contains("Etor test:")) {
	        		//System.out.println(str);
	        		double check = Double.parseDouble(str.split("Etor test:")[1]);
	        		if(check>=0.95) //check percentage needed to be adjusted/ justified.
	        		{
	        			isSimilar=true;
	        			similarFileCount=similarFileCount+1;
	        		}
	        		
	        	}
	        }	        
        
		} catch (IOException e) {

		   e.printStackTrace();

		  } finally {

		   try {
		    if (reader != null)
		    	reader.close();
		   } catch (IOException ex) {
		    ex.printStackTrace();
		   }
		  }
		
		if(isDuplicated) System.out.println(duplicatedFileCount + " files are duplicated!!!");
		if(isSimilar) System.out.println(similarFileCount + " files are similar!!!");
		
		return (isDuplicated||isSimilar);
	}

	/*function name: setUpFileExtensionList
	 * this method is to setUp file extension to copy
	 * this method is one of the methods to find soft forking (softForking) repo
	 * 
	 * Here is steps
	 * 1) download two repo
	 * 2) copy all source files into one folder for AC2
	 * 3) call AC2 for code similarity via AC2 UI
	 * 4) check whether repo is forking from original/main repo or not
	* */	
		public static void setUpFileExtensionList() {
			filesExtensionToCompare = new ArrayList<String>();
			filesExtensionToCompare.add(".json");
			filesExtensionToCompare.add(".js");
			filesExtensionToCompare.add(".java");			
		}
		
/*function name: checkForkingOrNot
 * this method is to check whether repo is forking from original/main repo or not
 * this method is one of the methods to find soft forking (softForking) repo
 * 
 * Here is steps
 * 1) download two repo
 * 2) copy all source files into one folder for AC2
 * 3) call AC2 for code similarity via AC2 UI
 * 4) check whether repo is forking from original/main repo or not
 * */
	public static void checkForkingOrNot(String PRRepo, String checkRepo) throws IOException {
		
		GHRepository repository = setUp(PRRepo);
		
		PagedIterable<GHRepository> forks= repository.listForks();		
		 Iterator<GHRepository> forker = forks.iterator();

	    boolean found=false;
	    while (forker.hasNext()) {
	    	GHRepository fork = forker.next();
	        if((("https://github.com/"+checkRepo).equals(fork.getHtmlUrl().toString()))) {
	    		found=true; break;
	    	}
	    }
	    if(!found) {
	    	repository = setUp(checkRepo);
	    	System.out.println("Soft Forking: " + checkRepo);
	    }
	 }	
	/*function name: softForkingMain
	 * this method is to check whether repo is forking from original/main repo or not
	 * this method is one of the methods to find soft forking (softForking) repo
	 * 
	 * Here is steps
	 * 1) download two repo
	 * 2) copy all source files into one folder for AC2
	 * 3) call AC2 for code similarity via AC2 UI
	 * 4) check whether repo is forking from original/main repo or not
	  * */	
		public static void softForkingMain(String PRRepo, String checkRepo) throws Exception {
			GHRepository mainrepo = setUp(PRRepo);
			//download two repo
			GitHubRepoDownload gd = new GitHubRepoDownload();
			gd.downloadRepoContent(mainrepo.getGitTransportUrl(), "test/"+mainrepo.getName()+"/");
			
			GHRepository cloneRepo = setUp(checkRepo);
			
			gd.downloadRepoContent(cloneRepo.getGitTransportUrl(), "test/"+cloneRepo.getName()+"/");
			
			//copy all source files into one folder for AC2
			//set up file extension to copy
			setUpFileExtensionList();
			if(filesExtensionToCompare==null) return;
			
			//for(String s: filesExtensionToCompare) {
				gd.copyFile("/test/"+mainrepo.getName()+"/", 
						"/test/ac2/", mainrepo.getName(),filesExtensionToCompare);
				
				gd.copyFile("/test/"+cloneRepo.getName()+"/", 
						"/test/ac2/", cloneRepo.getName(),filesExtensionToCompare);
			//}
			
			//call codeSimilarityUsingAC2
			boolean toCheck=codeSimilarityUsingAC2();
			//call checkForkingOrNot
			
			if(toCheck) checkForkingOrNot(PRRepo,checkRepo);
		}
/* function name: getSelfPromotionFinalByRetrieveAllLinksInPage
 * this method is to find the self promotion in the issues in given repo
 * this method can find all links in issue/pull request - body message , title message and comment for each issue/pull request
 * list all links by user name (eg; user1's message has 3 links, we record all 3 links under user1)
 * 
 * at the moment , ignore https://github.com/apps/dependabot
 * do not count for same project name (eg; for dvt/smallproject , sam/smallproject, project name is same (i.e., smallproject))
 * do not count for same owner name (eg; for dvt/smallproject , dvt/bigproject, current repo owner is same (i.e., dvt))
 * 
 * Here is step - to re-write
 * 1) check issue/pr includes other github repo link or not
 * 2) if there is github repo link, check whether the owner of comment is the contribute of promoted github link/ github repo or not
 * Note; manual check whether owner of comment mentions he is the contributor of mentioned github link in the comment or not
 */
		public static void getSelfPromotionFinalByRetrieveAllLinksInPage(String url, boolean issueType,String PRRepo, int checkNumber) throws IOException {
			String st ="https://github.com/";
			ArrayList<String> rr=new ArrayList<String>();
			HashMap<String,ArrayList<String>> userL = new HashMap<String,ArrayList<String>>();
			
			String repo2="";
			GHIssue is=null;
			GHPullRequest pr=null;
			GHRepository r1 = setUp(PRRepo);
			
			//r1 contribuor list
			PagedIterable<Contributor> r1Contributors = r1.listContributors();			
			Iterator<Contributor> itr = r1Contributors.iterator();
			ArrayList<String> r1CA=new ArrayList<String>();
		    while (itr.hasNext()) {
		    	r1CA.add(itr.next().getHtmlUrl().toString());
		    	}
		    
			if(issueType) {
				is = r1.getIssue(checkNumber);
				String userID=is.getUser().getHtmlUrl().toString().trim();
				if(r1CA.contains(userID)) {
					System.out.println("Contributor? "+userID);
					return; //is user is r1's contributor?
				}
				if(userID.equals("https://github.com/apps/dependabot")) return;
				rr = extractURL(is.getBody(),st,PRRepo,userID);				
				rr.addAll(extractURL(is.getTitle(),st,PRRepo,userID));
				userL.put(userID, rr);
				
				
  			List<GHIssueComment> commets=is.getComments();
				 for(GHIssueComment c: commets) {
					 String uID=c.getUser().getHtmlUrl().toString().trim();
					 if(!userID.equals(uID)){//is user is not issue or pull creator?
							//notCreator=true;
							continue;
						}
					 if(r1CA.contains(uID)) {//is user is r1's contributor?
						 continue;
					 }
					 if(uID.equals("https://github.com/apps/dependabot")) {
						 continue;
					 }
					 rr = extractURL(c.getBody(),st,PRRepo,uID);
					 //String uu=c.getUser().getHtmlUrl().toString();
						if(userL.get(uID)!=null) {
							rr.addAll(userL.get(uID));
							userL.put(uID, rr);
						}else {
							userL.put(uID, rr);
						}
				 	 }
			  
			}else {
				pr = r1.getPullRequest(checkNumber);
				String userID=pr.getUser().getHtmlUrl().toString().trim();
				if(r1CA.contains(userID)) {
					System.out.println("Contributor? "+userID);
					return;//is user is r1's contributor?
				}
				if(userID.equals("https://github.com/apps/dependabot")) return;
				rr = extractURL(pr.getBody(),st,PRRepo,userID);				
				rr.addAll(extractURL(pr.getTitle(),st,PRRepo,userID));
				userL.put(userID, rr);					
  			List<GHIssueComment> commets=pr.getComments();
				 for(GHIssueComment c: commets) {
					 String uu=c.getUser().getHtmlUrl().toString().trim();
					 if(!userID.equals(uu)){//is user is not issue or pull creator?
							//notCreator=true;
							continue;
						}
					 if(r1CA.contains(uu)) {//is user is r1's contributor?
						 continue;
					 }
					 if(uu.equals("https://github.com/apps/dependabot")) {
						 continue;
					 }
					 rr = extractURL(c.getBody(),st,PRRepo,uu);
						if(userL.get(uu)!=null) {
							rr.addAll(userL.get(uu));
							userL.put(uu, rr);
						}else {
							userL.put(uu, rr);
						}
				 	 }
			}
			
			//removed duplicated
			if(userL.size()>0) {
				HashMap<String,ArrayList<String>> aa = new HashMap<String,ArrayList<String>>();
				for (Map.Entry<String, ArrayList<String>> me2 : userL.entrySet()) {
					 ArrayList<String> tmpA=me2.getValue();
					 //System.out.println(tmpA);
					 Set<String> set = new HashSet<>(tmpA);
					 tmpA.clear();
					 tmpA.addAll(set);
					 aa.put(me2.getKey(), tmpA);
				}
				
				userL=aa;
			}
			 
			
			if(userL.size()>0) {				
				for (Map.Entry<String, ArrayList<String>> me2 : userL.entrySet()) {
					try {
					  String u=me2.getKey();
					  //System.out.println(u);
					  ArrayList<String> ls = me2.getValue(); 
					  for(String s : ls) {						  
						  //System.out.println(s+found);
						  //System.out.println(s);
						boolean found = false;
						boolean isDemo=false;
						boolean isUserURL = false;
						boolean isSameRepo = false;
						  boolean skip=false;
						  if(s.startsWith("DEMO")) {
							  repo2=s.replace("DEMO", "");
							  isDemo=true;skip=true;
							  //continue;
						  }
						  if(s.startsWith("USERURL")) {
							  repo2=s.replace("USERURL", "");
							  isUserURL=true;skip=true;
							  //continue;
						  }
						  if(s.startsWith("SAMEREPO")) {
							  repo2=s.replace("SAMEREPO", "");
							  isSameRepo=true;skip=true;
							  //continue;
						  }
						  if(!skip){try {
								GHRepository r2 = setUp(s);
								PagedIterable<Contributor> contributors = r2.listContributors();		
							    Iterator<Contributor> iterator = contributors.iterator();
							    while (iterator.hasNext()) {
							    	String aa=iterator.next().getHtmlUrl().toString();
							    	//System.out.println(aa);
							    	if(aa.equals(u)){
							    		//System.out.println("here");
							    		repo2=s;
							    		//user2=u;
							    		found = true; 
							    		break;					    		
							    }
							}}catch(Exception e) {
								//e.printStackTrace();
								continue;
							}}
						  //if(found) 
							  System.out.println("found =" + found +", i/pr : " + url +", user: "+u 
									  +", repo2: "+repo2+", isDemo: "+isDemo+", isUSERURL: "+isUserURL
									  +", isSameRepo: "+isSameRepo);
					     }
					  }catch(Exception e) {
						 //e.printStackTrace();
			        	 continue;
			         }
				}}
			/*for (Map.Entry<String, String> me2 : result.entrySet()) {
			  System.out.println("found =" + true +", i/pr : " + url +", user: "+me2.getKey() +", repo2: "+me2.getValue());
	         }*/ 
			}
		
		public static ArrayList<String> extractURL(
		        String str, String st, String o,String u)
		    {
			
			ArrayList<String> list
		            = new ArrayList<>();
		  
		        String regex
		            = "\\b((?:https?|ftp|file):"
		              + "//[-a-zA-Z0-9+&@#/%?="
		              + "~_|!:, .;]*[-a-zA-Z0-9+"
		              + "&@#/%=~_|])";
		  
		        Pattern p = Pattern.compile(
		            regex,
		            Pattern.CASE_INSENSITIVE);
		        if(str==null) return list;
		        Matcher m = p.matcher(str);
		  
		         while (m.find()) {
		        	String tmp=str.substring(
			                m.start(0), m.end(0));
		        	
		        	//System.out.println("tmp: " + tmp);
		        	
		        	//does link refers to repo2 issue / pull request?
		        	if((tmp.contains("/issues/") || tmp.contains("/pull/") || tmp.contains("/commit/")
		        			|| tmp.contains("/tree/") || tmp.contains("/releases/") || tmp.contains("/blob/")
		        			|| tmp.contains("/runs/") ) 
		        		&& tmp.split("/").length>4)
		        	{
		        		tmp = tmp.split("/")[3].trim()+"/"+tmp.split("/")[4];
		        		String rt="DEMO"+tmp;
		        		if(!list.contains(rt)) list.add(rt);
		        		continue;
		        	}		        	
		        	
		        	//does link include userID in repo2 ?
		        	else if(tmp.contains(u) && tmp.split("/").length>4) {
		        		tmp = tmp.split("/")[3].trim()+"/"+tmp.split("/")[4];
		        		String rt="USERURL"+tmp;
		        		if(!list.contains(rt)) list.add(rt);
		        		continue;
		        	}
		        	
		        	else if(tmp.contains(st) && tmp.split("/").length>4)
		            {
		        		tmp = tmp.split("/")[3].trim()+"/"+tmp.split("/")[4];
		        		//System.out.println(tmp);
		        		
		        		//check the link lenght , 
		        		if(tmp.split("/").length<2) {
		        			continue;
		        		}
		        		
		        		//does link refers back to repo1?
		        		if(o.split("/")[0].equals(tmp.split("/")[0])) {
		        			String rt="SAMEREPO"+tmp;
			        		if(!list.contains(rt)) list.add(rt);
		        			continue;
		        		}
		        		
		        		//does link includes same project name?
		        		//if(o.split("/")[1].equals(tmp.split("/")[1])) {
		        			//continue;
		        		//}
		        		
		            	if(!list.contains(tmp)) list.add(tmp);
		            }
		        }
		        
		        return list;
		    }
		


/*function name: checkInChangeLog
 * this method is to step 7)
 * this method is one of the methods to find uninformed license change / check whether license file is changed. if change, did they have PR? if no PR/ no issues, meaning "no awareness"
 * 
 * Here is the step :
 * 
 * Run "unInformedLicenseChangeMain" function
 * 1) retrieve license file name
 * 2) write into Repo.txt
 * 
 * Run "runScript" function
 * 3) call ""
 * 4) C:\Users\Etor\Desktop\Crawler\GetFileUpdatedDate.py 
 * 5) python file retrieve (a) date when license file is change (b) author who changed license (c) sha of commit
 * 6) write into CommitList.txt
 * 
 * Run "commitChangeLicenseChecking - after running - copy only with "true" and paste into CommitListTest-LicenseChange-toRun.txt
 * 7) check whether commit includes license change
 * 
 * Read data from CommitList.txt file and 
 * 8) call checkInChangeLog function to check in change log whether license change is informed or not * 
 * if not found, 
 * 9) call "getPullReqeustOfCommit" function to get pull request by SHA 
 * */

public static String checkInChangeLog(String repoStr, HashMap<String,String>  licenseList){
	try {
	GHRepository repo = setUp(repoStr);
	GHContent changeLog=repo.getFileContent("CHANGELOG.md");
	if(changeLog!=null) {
		InputStream  is = changeLog.read();
		BufferedReader reader = null;
		try{
		reader = new BufferedReader(new InputStreamReader(is));
		String str=""; 	                   
	        while ((str = reader.readLine()) != null) { 
	        	String keyFound = checkInHashMap(licenseList,str);
	        	return keyFound;
	        }
		}catch(Exception e) {}finally {reader.close();}
	}}catch(Exception e) {
		
	}
	return "";
}

/*function name: runScript
 * this method is to step 7)
 * this method is one of the methods to find uninformed license change / check whether license file is changed. if change, did they have PR? if no PR/ no issues, meaning "no awareness"
 * 
 * Here is the step :
 * 
 * Run "unInformedLicenseChangeMain" function
 * 1) retrieve license file name
 * 2) write into Repo.txt
 * 
 * Run "runScript" function
 * 3) call ""
 * 4) C:\Users\Etor\Desktop\Crawler\GetFileUpdatedDate.py 
 * 5) python file retrieve (a) date when license file is change (b) author who changed license (c) sha of commit
 * 6) write into CommitList.txt
 * 
 * Run "commitChangeLicenseChecking - after running - copy only with "true" and paste into CommitListTest-LicenseChange-toRun.txt
 * 7) check whether commit includes license change
 * 
 * Read data from CommitList.txt file and 
 * 8) call checkInChangeLog function to check in change log whether license change is informed or not * 
 * if not found, 
 * 9) call "getPullReqeustOfCommit" function to get pull request by SHA 
 * */
public static boolean getPullReqeustOfCommit(String repoStr, String sha) throws IOException {
	GHRepository repo = setUp(repoStr);
	GHCommit commit=repo.getCommit(sha);
	if(commit!=null) {
	PagedIterable<GHPullRequest> prs = commit.listPullRequests();
	Iterator<GHPullRequest> gc = prs.iterator();
	int count=0;
	while (gc.hasNext()) {
    	GHPullRequest gcP = gc.next();
    	System.out.println("Pull request - "+ gcP.getHtmlUrl()+", repo - " + repoStr + ": Informed!!!");
    	count++;
    }
	if(count==0) {
		System.out.println(repoStr +": Uninformed!!! "+commit.getHtmlUrl());
		return false;
		}
	}else
	{
		System.out.println(repoStr +": ???!!! "+sha);
	}
	return true;
}

/*function name: runScript
 * this method is to step 3) 4) 5) 6)
 * this method is one of the methods to find uninformed license change / check whether license file is changed. if change, did they have PR? if no PR/ no issues, meaning "no awareness"
 * 
 * Here is the step : 
 * 
  * Run "unInformedLicenseChangeMain" function
 * 1) retrieve license file name
 * 2) write into Repo.txt
 * 
 * Run "runScript" function
 * 3) call ""
 * 4) C:\Users\Etor\Desktop\Crawler\GetFileUpdatedDate.py 
 * 5) python file retrieve (a) date when license file is change (b) author who changed license (c) sha of commit
 * 6) write into CommitList.txt
 * 
 * Run "commitChangeLicenseChecking - after running - copy only with "true" and paste into CommitListTest-LicenseChange-toRun.txt
 * 7) check whether commit includes license change
 * 
 * Read data from CommitList.txt file and 
 * 8) call checkInChangeLog function to check in change log whether license change is informed or not * 
 * if not found, 
 * 9) call "getPullReqeustOfCommit" function to get pull request by SHA 
 */
private static void runScript(){
  Process process;
  System.out.println("runScript");
	    try{
	         // process = Runtime.getRuntime().exec(new String[]{file,"arg1","arg2"});
	          process = Runtime.getRuntime().exec("tool/PyGithub.bat");
	    }catch(Exception e) {
	       System.out.println("Exception Raised" + e.toString());
	    }
	    
	}

/*function name: retrieveLicenseChange
 * this method is to read the unethical list file (keyword: ethic) (i.e., generated by Python script) and write the repo list into file 
 * this method is one of the methods to find uninformed license change / check whether license file is changed. if change, did they have PR? if no PR/ no issues, meaning "no awareness"
 * 
 * Here is the step :
 * 
 * Run "unInformedLicenseChangeMain" function
 * 1) retrieve license file name
 * 2) write into Repo.txt
 * 
 * Run "runScript" function
 * 3) call ""
 * 4) C:\Users\Etor\Desktop\Crawler\GetFileUpdatedDate.py 
 * 5) python file retrieve (a) date when license file is change (b) author who changed license (c) sha of commit
 * 6) write into CommitList.txt
 * 
 * Run "commitChangeLicenseChecking - after running - copy only with "true" and paste into CommitListTest-LicenseChange-toRun.txt
 * 7) check whether commit includes license change
 * 
 * Read data from CommitList.txt file and 
 * 8) call checkInChangeLog function to check in change log whether license change is informed or not * 
 * if not found, 
 * 9) call "getPullReqeustOfCommit" function to get pull request by SHA 
 **/

public static void retrieveLicenseChange(String tmpRepo) {

	  try {
	  FileWriter myWriter = new FileWriter("Repo.txt");
			try {
					GHRepository repo=setUp(tmpRepo);
					String toWrite="";
					if(repo.getLicenseContent()!=null) {
						toWrite=tmpRepo+" "+repo.getLicenseContent().getName();
						myWriter.write(toWrite+"\n");
					}
				//System.out.println(toWrite);
			     
			}catch(Exception e) {
				e.printStackTrace();
			} finally {
				try {
					 myWriter.close();
				}catch(Exception e) {}
			  }

	  		} catch (IOException e) {

	  		}
}

/*function name: commitChangeLicenseChecking
 * this method is to step 7)
 * this method is one of the methods to find uninformed license change / check whether license file is changed. if change, did they have PR? if no PR/ no issues, meaning "no awareness"
 * 
 * Here is the step :
 * 
 * Run "unInformedLicenseChangeMain" function
 * 1) read from file for repo name of ethical/unethical issue list
 * 2) retrieve license file name
 * 3) write into Repo.txt
 * 
 * Run "runScript" function
 * 3) call ""
 * 4) C:\Users\Etor\Desktop\Crawler\GetFileUpdatedDate.py 
 * 5) python file retrieve (a) date when license file is change (b) author who changed license (c) sha of commit
 * 6) write into CommitList.txt
 * 
 * Run "commitChangeLicenseChecking - after running - copy only with "true" and paste into CommitListTest-LicenseChange-toRun.txt
 * 7) check whether commit includes license change
 * 
 * Read data from CommitList.txt file and 
 * 8) call checkInChangeLog function to check in change log whether license change is informed or not * 
 * if not found, 
 * 9) call "getPullReqeustOfCommit" function to get pull request by SHA 
 **/
public static void commitChangeLicenseChecking() {
	int i=0;
	boolean lchange=false;
	String l1="";
	String l2="";
	HashMap<String,String> licenseList = setupLicenseList();
	  BufferedReader objReader = null;
	  try {
	   String strCurrentLine="";
	   objReader = new BufferedReader(new FileReader("CommitListTest.txt"));
	   while ((strCurrentLine = objReader.readLine()) != null) {
		   try {
			   lchange=false;
			   String repoStr = strCurrentLine.split("\\|")[0];
			   String sha = strCurrentLine.split("\\|")[5];
			   String lic = strCurrentLine.split("\\|")[1];
			   GHRepository repo = setUp(repoStr);
			   GHCommit commit=repo.getCommit(sha);
			   List<File> fl = commit.getFiles();
			   for(GHCommit.File f: fl) {
				   if(f.getFileName().equals(lic)) {
					   String sss=f.getPatch();
					   char[] ca=sss.toCharArray();
					   String removeC = "";
					   String addC="";
					   boolean adding = false;
					   boolean removing = false;
					   for(char c:ca) {
					   if(c=='+') {
						   adding=true;
						   removing=false;
					   }
					   if(c=='-') {
						   removing = true;
						   adding=false;
					   }
					   if(adding) {
						   addC+=c;
					   }
					   if(removing) {
						   removeC+=c;
					   }
				    }
					   if(removeC.trim().length()>0 && addC.trim().length()>0)
					   { 
						  addC=addC.replaceAll("\\+", "").replaceAll("\\r|\\n", "").replaceAll("\\s{2,}", " ").trim();
						  removeC=removeC.replaceAll("\\+", "").replaceAll("\\r|\\n", "").replaceAll("\\s{2,}", " ").trim();
						  l1= checkInHashMapWithoutVersion(licenseList,addC);
						  l2= checkInHashMapWithoutVersion(licenseList,removeC);
						  if(l1!=null && l2!=null && !l1.equals(l2)) {
							 lchange=true; 
						  }
					   }
				   }
			   }
			   i=i+1;
			   if(i%250==0) {
					Thread.sleep(25000);
				}
			 System.out.println("lChange = "+ lchange + ", "+strCurrentLine + ", added lic = "+ l1 + ", removed lic = "+l2);
	    }catch(Exception e) {
	    	//e.printStackTrace();
	    	System.out.println("to copy error: "+strCurrentLine);
	     }
	   }
	   
	  } catch (IOException e) {
		  e.printStackTrace();
	  } finally {
	   try {
	    if (objReader != null)
	     objReader.close();
	   } catch (IOException ex) {
	    ex.printStackTrace();
	   }
	  }  	
}

/*function name: unInformedLicenseChangeMain
 * this method is to retrieve the main function to call all other functions for uninformed license change
 * this method is one of the methods to find uninformed license change / check whether license file is changed. if change, did they have PR? if no PR/ no issues, meaning "no awareness"
 * 
 * Here is the step :
 * Note; so far, run the functions one by one (i.e., retrieveLicenseChange(tmpRepo); runScript(); commitChangeLicenseChecking(); unInformedLicenseChangeMain(String tmpRepo);)
 * Do not forget to change path in .bat and .py
 *  
 * Run "unInformedLicenseChangeMain" function
 * 1) retrieve license file name
 * 2) write into Repo.txt
 * 
 * Run "runScript" function
 * 3) call ""
 * 4) C:\Users\Etor\Desktop\Crawler\GetFileUpdatedDate.py 
 * 5) python file retrieve (a) date when license file is change (b) author who changed license (c) sha of commit
 * 6) write into CommitList.txt
 * 
 * Run "commitChangeLicenseChecking - after running - copy only with "true" and paste into CommitListTest-LicenseChange-toRun.txt
 * 7) check whether commit includes license change
 * 
 * Read data from CommitList.txt file and 
 * 8) call checkInChangeLog function to check in change log whether license change is informed or not * 
 * if not found, 
 * 9) call "getPullReqeustOfCommit" function to get pull request by SHA 
 * 
 * */

public static void unInformedLicenseChangeMain(String tmpRepo) throws Exception {
	//retrieveLicenseChange(tmpRepo);
	//runScript();
	//commitChangeLicenseChecking();
	
	boolean ret=true;
	HashMap<String,String> licenseList = setupLicenseList();
	  BufferedReader objReader = null;
	  try {
	   String strCurrentLine;
	   objReader = new BufferedReader(new FileReader("CommitListTest.txt"));
	   while ((strCurrentLine = objReader.readLine()) != null) {
		   strCurrentLine = strCurrentLine.trim();
		   try {
		   //System.out.println("Mojave-Sun/mojave-sun-13|LICENSE|2017-11-20 18:29:12|2017-11-20 18:29:12|GitAuthor(name=\"Jordan Brown\")|7cf0ba891695e01ffb3ee29bccfaa4925eb2070d".split("\\|")[1]);
		   String retK=null;
		   retK=checkInChangeLog(strCurrentLine.split("\\|")[0],licenseList);
		   if(retK==null || retK.trim().length()==0) {
			   ret=getPullReqeustOfCommit(strCurrentLine.split("\\|")[0],strCurrentLine.split("\\|")[5]);
			  }
	    }catch(Exception e) {
	    	System.out.println("to copy error: "+strCurrentLine);
	   }}
	  } catch (IOException e) {
		  //e.printStackTrace();
	  } finally {
	   try {
	    if (objReader != null)
	     objReader.close();
	   } catch (IOException ex) {
	    ex.printStackTrace();
	   }
	  }  

}

/*function name: retrieveAllFilesFromRepo
 * this method is to retrieve directory of the repo
 * this method is one of methods to find the license inconsistency.
 * */
public static void retrieveAllFilesFromRepo(String repo, GHContent tmp,HashMap<String,String> licenseList, 
		String licenseKey, GHRepository repository ) throws IOException {
	PagedIterable<GHContent> gc2= tmp.listDirectoryContent();
	Iterator<GHContent> gc = gc2.iterator();
    while (gc.hasNext()) {
    	GHContent gcc = gc.next();
    	if(gcc.isDirectory()) {
			retrieveAllFilesFromRepo(repo,gcc,licenseList,licenseKey,repository);
			}
		else {
			 if (gcc.isFile()) {
					if(licenseComparison(licenseList,licenseKey,gcc)==1){
						System.out.println("License Inconsistency found in Repo : (" + repository.getHtmlUrl()+")");
						return;						
					}
				}
			}
        }
    }

/*function checkInHashMap
 * this method is to check String in HashMap
 * this method is one of methods to find the license inconsistency.
 * */

public static String checkInHashMap(HashMap<String,String> licenseList, String line) {
	String keyFound=null;
	// Getting a Set of Key-value pairs
	Set entrySet = specialLicenseList.entrySet();	
		 // Obtaining an iterator for the entry set
	Iterator it = entrySet.iterator();	
		 // Iterate through HashMap entries(Key-Value pairs)
		 //System.out.println("HashMap Key-Value Pairs : ");
		 while(it.hasNext()){
		    Map.Entry<String,String> me = (Entry<String, String>)it.next();
		    if(line.toUpperCase().contains(" "+me.getValue().toUpperCase()+" "))
		    	{
		    	//System.out.println("here : " + me.getKey());
			    	keyFound=me.getKey(); 
			    	break;
		    	}
		    else if(line.toUpperCase().contains("\""+me.getValue().toUpperCase()+"\""))
	    	{
	    	//System.out.println("here : " + me.getKey());
		    	keyFound=me.getKey(); 
		    	break;
	    	}
		}
	//System.out.println("here keyfound: " + keyFound);	 
	if(keyFound!=null && keyFound.trim().length()>0) return keyFound;
		 
	 // Getting a Set of Key-value pairs
	 entrySet = licenseList.entrySet();	
	 // Obtaining an iterator for the entry set
	 it = entrySet.iterator();	
	 // Iterate through HashMap entries(Key-Value pairs)
	 //System.out.println("HashMap Key-Value Pairs : ");
	 while(it.hasNext()){
	    Map.Entry<String,String> me = (Entry<String, String>)it.next();
	    if(line.toUpperCase().replaceAll("[^a-zA-Z0-9]", "")
	    	.contains(me.getValue().toUpperCase().replaceAll("[^a-zA-Z0-9]", "")))
	    	{
	    	//System.out.println("here : " + me.getKey());
		    	keyFound=me.getKey(); 
		    	break;
	    	}
	    
	}
	 return keyFound;
}

public static String checkInHashMapWithoutVersion(HashMap<String,String> licenseList, String line) {
	String keyFound=null;
	// Getting a Set of Key-value pairs
	Set entrySet = specialLicenseList.entrySet();	
		 // Obtaining an iterator for the entry set
	Iterator it = entrySet.iterator();	
		 // Iterate through HashMap entries(Key-Value pairs)
		 //System.out.println("HashMap Key-Value Pairs : ");
		 while(it.hasNext()){
		    Map.Entry<String,String> me = (Entry<String, String>)it.next();
		    if(line.toUpperCase().contains(" "+me.getValue().toUpperCase()+" "))
		    	{
		    	//System.out.println("here : " + me.getKey());
			    	keyFound=me.getKey(); 
			    	break;
		    	}
		    else if(line.toUpperCase().contains("\""+me.getValue().toUpperCase()+"\""))
	    	{
	    	//System.out.println("here : " + me.getKey());
		    	keyFound=me.getKey(); 
		    	break;
	    	}
		}
	//System.out.println("here keyfound: " + keyFound);	 
	if(keyFound!=null && keyFound.trim().length()>0) return keyFound;
		 
	 // Getting a Set of Key-value pairs
	 entrySet = licenseList.entrySet();	
	 // Obtaining an iterator for the entry set
	 it = entrySet.iterator();	
	 // Iterate through HashMap entries(Key-Value pairs)
	 //System.out.println("HashMap Key-Value Pairs : ");
	 while(it.hasNext()){
	    Map.Entry<String,String> me = (Entry<String, String>)it.next();
	    int end=me.getValue().length();
	    if(me.getValue().indexOf("License")>0) {
	    	end=me.getValue().indexOf("License");
	    }
	    if(me.getValue().indexOf("license")>0) {
	    	end=me.getValue().indexOf("license");
	    }
	    String tmp=me.getValue().substring(0,end);
	    //System.out.println(tmp);
	    if(line.toUpperCase().replaceAll("[^a-zA-Z0-9]", "")
	    	.contains(tmp.toUpperCase().replaceAll("[^a-zA-Z0-9]", "")))
	    	{
	    	//System.out.println("here : " + me.getKey());
		    	keyFound=me.getKey(); 
		    	break;
	    	}
	    
	}
	 return keyFound;
}

/*function licenseComparison
 * this method is to compare the license.
 * this method is one of methods to find the license inconsistency.
 */

public static int licenseComparison(HashMap<String,String> licenseList, String licKey, GHContent tmp) throws IOException {
	int isSame=2; // 0 = same , 1 = different , 2 = not found
	String keyFound="";
	//text.replaceAll("[^a-zA-Z0-9]", "");
	InputStream  is = tmp.read();
	BufferedReader reader = null;
	try{
	reader = new BufferedReader(new InputStreamReader(is));
	String str=""; 
                   
        while ((str = reader.readLine()) != null) {    
        	keyFound=checkInHashMap(licenseList,str);
        	//System.out.println("here: " + keyFound);
        	if(keyFound!=null && keyFound.trim().length()>0) {
        		if(keyFound.equals(licKey)) {
        			isSame = 0;
        		}else {
        			isSame=1;
        			//System.out.println("hello: "+specialLicenseList.get(licKey));
        			System.out.println("Repo License: ("
        			+(licenseList.get(licKey)!=null?licenseList.get(licKey):specialLicenseList.get(licKey))+")");
        			System.out.println("Different license : (" 
        			+(licenseList.get(keyFound)!=null?licenseList.get(keyFound):specialLicenseList.get(keyFound))
        			+ ") in (" + tmp.getPath() +")");
        		}
        		break;
        	}
        } 
        
      }catch(Exception e) {}finally {reader.close();}
	return isSame;
}

/*function name: setupLicenseList
 * this method is to setup the license type in HashMap. 
 * this method is one of methods to find the license inconsistency.
 */

public static HashMap<String,String> setupLicenseList() {
	
	//special license setup
	specialLicenseList = new HashMap<String,String>();
	specialLicenseList.put("mit", "MIT");
	specialLicenseList.put("isc", "ISC");

	HashMap<String,String> licenseList=null;
	
	  BufferedReader objReader = null;
	  try {
	   String strCurrentLine="";
	   licenseList = new HashMap<String,String>();
	   objReader = new BufferedReader(new FileReader("tool/LicenseList.txt"));

	   while ((strCurrentLine = objReader.readLine()) != null) {
		   licenseList.put(strCurrentLine.split(",")[1],strCurrentLine.split(",")[0]);
	   }
	  } catch (IOException e) {

	   e.printStackTrace();

	  } finally {

	   try {
	    if (objReader != null)
	     objReader.close();
	   } catch (IOException ex) {
	    ex.printStackTrace();
	   }
	  }
	  
	  return licenseList;

}

/*function name:licenseInconsistency
 * this method is to call setupLicenseList(), to get repo license type, to call licenseComparison(HashMap,String);
 * this method is one of methods to find the license inconsistency.
 */

public static void licenseInconsistency(String PRRepo) throws Exception {
	HashMap<String,String> licenseList = setupLicenseList();
	if(licenseList==null) return;
	GHRepository repository = setUp(PRRepo);
	//System.out.println(repository.getLicense().getKey());
	String licenseKey=repository.getLicense()!=null?repository.getLicense().getKey():null;
	if(licenseKey!=null && licenseKey.trim().length()>0 
		&& (specialLicenseList.get(licenseKey)!=null || licenseList.get(licenseKey)!=null)) {
		
		//check for read me
		/*boolean readMeOk=repository.getReadme()!=null && repository.getReadme().isFile() && repository.getReadme().read()!=null;
		if(readMeOk && licenseComparison(licenseList,licenseKey,repository.getReadme())==1) {
			System.out.println("License Inconsistency found in Repo : (" + repository.getHtmlUrl()+")");
			return;
		}*/
		//check for source code
		List<GHContent> tmp;
		try {
		tmp = repository.getDirectoryContent(".");		
		if(tmp!=null) {
			for(int i=0;i<tmp.size();i++) {
				try {
				//System.out.println(tmp.get(i).getHtmlUrl().replace("https://github.com/"+PRRepo, ""));
				//System.out.println(tmp.get(i).getName());
					if(tmp.get(i).isDirectory()) {
						retrieveAllFilesFromRepo(PRRepo, tmp.get(i),licenseList,licenseKey,repository);
					}
					else if (tmp.get(i).isFile()) {
						if(licenseComparison(licenseList,licenseKey,tmp.get(i))==1){
							//System.out.println("here...");
							System.out.println("License Inconsistency found in Repo : (" + repository.getHtmlUrl()+")");
							return;						
						}
					}
				}catch(Exception e) {
				System.out.println("error while reading directory content in inner loop "+repository.getHtmlUrl()+", Lic:"+licenseKey);
				e.printStackTrace();
				//continue;
				}
			}}
		}catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("error while reading directory content outter loop "+repository.getHtmlUrl()+", Lic:"+licenseKey);
			e.printStackTrace();
		}
		///////////////////////
	}
	
}
/**/

/*function name: noLicenseRepo
 * this method is to check whether repository has license or not */
public static void noLicenseRepo(String PRRepo) throws IOException {
	boolean licStringInReadMe=false;
	boolean liceFile=false;
	boolean noSource=true;
	HashMap<String,String> licenseList = setupLicenseList();
	int count=0;
	if(licenseList==null) return;
	GHRepository repository = setUp(PRRepo);
	if(repository.isFork()) {
		System.out.println("forked repo: " + PRRepo);
		return;
	}
	//System.out.println(repository.getLicense().getKey());
	if(repository.getLicenseContent()!=null) {
		count=count+1;
	}
	//check for read me
	if(count==0) {
	boolean readMeOk=repository.getReadme()!=null && repository.getReadme().isFile() && repository.getReadme().read()!=null;
	if(readMeOk) {
	InputStream  is = repository.getReadme().read();
	BufferedReader reader = null;
	try{
	reader = new BufferedReader(new InputStreamReader(is));
	String str=""; 
    String keyFound=null;               
        while ((str = reader.readLine()) != null) {    
        	keyFound=checkInHashMap(licenseList,str);
        	//System.out.println("here: " + keyFound);
        	if(keyFound!=null && keyFound.trim().length()>0) {
        		count=count+1;
        		break;
        	}
        	if(str.toUpperCase().contains("LICENSE")) licStringInReadMe=true;
        } 
        
      }catch(Exception e) { e.printStackTrace();}finally {reader.close();}}}
	if(count==0) {
		List<GHContent> tmp = repository.getDirectoryContent(".");
		for(GHContent g:tmp) {
			if(g.getName().toUpperCase().contains("LICENSE")) {
				liceFile=true;
			}
			if(!(FilenameUtils.getExtension(g.getName().toUpperCase()).contains("TXT")) &&
			   !(FilenameUtils.getExtension(g.getName().toUpperCase()).contains("MD")) &&
			   !(FilenameUtils.getExtension(g.getName().toUpperCase()).contains("PNG")) &&
			   !(FilenameUtils.getExtension(g.getName().toUpperCase()).contains("PDF"))) {
				noSource=false;
			}
		}
	}
	if(count==0) System.out.println("liceFile: "+liceFile+", licStringInReadMe: "+licStringInReadMe+" No license in Repo!!! " + PRRepo +", noSource= " + noSource );	
}

public static String concatLinkTmpHyphen(String st, String keyword) {	    
    List<String> list = new ArrayList<>();
    //String regexString = "\\b(https://|www[.-])[A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]";
    //^https?:\/\/\w+(?:[.-]\w+)*(?::[0-9]+)?\/?$
    String regexString = "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)";
    Pattern pattern = Pattern.compile(regexString,Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(st);
    while (matcher.find()) {
  	  list.add(st.substring(matcher.start(0),matcher.end(0)));
    }
for(String str:list)
  if(str.contains(keyword)) return str;

return"";
	
}

public static void main(String[] args){
		try {
			Instant start = Instant.now();
			if(args==null || args.length<5)
				System.exit(0);			
			token = args[0];
			int type = Integer.parseInt(args[1]);
			String repo1 = args[2].equals("null")?"":args[2].split("/")[3].trim()+"/"+args[2].split("/")[4];
			String repo2 = args[3].equals("null")?"":args[3].split("/")[3].trim()+"/"+args[3].split("/")[4];
			String i = args[4].equals("null")?"":args[4].split("/")[(args[4].split("/").length)-1];
			String iT = args[4].equals("null")?"":args[4].split("/")[(args[4].split("/").length)-2];
			
			
			switch(type) {
			//1.Unmaintained
			case 1: UnmaintainedAndroidWithPaidService(repo1);
			break;
					
			//2.NoLicense
			case 2: noLicenseRepo(repo1);
			break;
			
			//3.LicIn
			case 3: licenseInconsistency(repo1);
			break;
			
			//4.Uninfored
			case 4: unInformedLicenseChangeMain(repo1);//commitChangeLicenseChecking();//runScript();//retrieveLicenseChange(repo1);//
			break;
			
			//5.NoAtt
			case 5: noAttributionCheckInCode(repo1,iT.startsWith("i"),Integer.parseInt(i));
			break;
			
			//6.Soft-Forking
			case 6: softForkingMain(repo1,repo2);
			break;
			
			//7.SelfPro
			case 7: getSelfPromotionFinalByRetrieveAllLinksInPage(args[4], iT.startsWith("i"), repo1, Integer.parseInt(i));
			break;
			}
			
			Instant end=Instant.now();
			Duration timeE = Duration.between(start,end);
			System.out.println("Time taken = "+ timeE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Here");
			e.printStackTrace();
		}
	}

}
