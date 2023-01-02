package myUtils;

public class MyDoc {
    private String id = "";
    private String authors = "";
    private String date = "";
    private String content = "";
    private String keywords = "";
    private String title = "";
    private String entrydate = "";
    private String abstractText = "";
    private String references = "";

    @Override
    public String toString() {
        String docInfo = "MyDoc{"
                + "\n\tId: " + id
                + "\n\tTitle: " + title
                + "\n\tAbstractText: " + abstractText
                + "\n\tKeywords: " + keywords
                + "\n\tAuthors: " + authors
                + "\n\tContent: " + content
                + "\n\tDate: " + date
                + "\n\tEntrydate: " + entrydate
                + "\n\tReferences: " + references;                
        return docInfo + "\n}";
    }

    public MyDoc(){}

    public String getId(){return this.id;}

    public void setId(String id){this.id = id;}

    public String getAuthors(){return this.authors;}

    public void setAuthors(String authors){this.authors = authors;}

    public String getDate(){return this.date;}

    public void setDate(String date){this.date = date;}

    public String getContent(){return this.content;}

    public void setContent(String content){this.content = content;}

    public String getKeywords(){return this.keywords;}

    public void setKeywords(String keywords){this.keywords = keywords;}

    public String getTitle(){return this.title;}

    public void setTitle(String title){this.title = title;}

    public String getEntrydate(){return this.entrydate;}

    public void setEntrydate(String entrydate){this.entrydate = entrydate;}

    public String getAbstractText(){return this.abstractText;}

    public void setAbstractText(String abstractText){this.abstractText = abstractText;}

    public String getReferences(){return this.references;}

    public void setReferences(String references){this.references = references;}
}
