package ie.delilahsthings.soothingloop;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public abstract class ProfileManager {

    final private static Pattern profileNameValidationPattern = Pattern.compile("\\s*(.*[\\S])\\s*");
    final private static String ProfileExtension = ".xml";
    final private static int ProfileExtensionLength = 4;

    static String getProfilePath()
    {
        return StaticContext.getAppContext().getFilesDir()+"/profiles/";
    }
    static File getProfileFile(String filename) {
        new File(getProfilePath()).mkdirs();
        return new File(getProfilePath()+filename+ProfileExtension);
    }
    static File getDefaultProfileFile() {
        return new File(StaticContext.getAppContext().getFilesDir()+"/default_profile"+ProfileExtension);
    }

    public static boolean deleteProfile(String profileName)
    {
        String path = getProfilePath()+"/"+profileName+ProfileExtension;
        return new File(path).delete();
    }

    public static void exportProfile(String exportedProfileName, Uri uri) throws ProfileIOException {
        try {
            Context ctx = StaticContext.getAppContext();
            OutputStream out = ctx.getContentResolver().openOutputStream(uri);
            File in = getProfileFile(exportedProfileName);
            saveProfile(out, loadProfile(in));
        }
        catch (IOException ex) {
            throw new ProfileSaveException(ex);
        }
    }

    public static boolean importProfile(String profileName, Uri uri) {
        try {
            Context ctx = StaticContext.getAppContext();
            File out = getProfileFile(profileName);
            InputStream in = ctx.getContentResolver().openInputStream(uri);
            saveProfile(out, loadProfile(in));
            return true;
        }
        catch (ProfileIOException | IOException ex) {
            return false;
        }
    }

    public static String[] listProfiles()
    {
        ArrayList<String> files = new ArrayList();
        String path = getProfilePath();
        for(String file: Util.safe_list(new File(path))) {
            files.add(file.substring(0, file.length() - ProfileManager.ProfileExtensionLength));
        }
        return files.toArray(new String[0]);
    }

    public static Profile loadProfile(String profileName) throws ProfileLoadException {
        return loadProfile(getProfileFile(profileName));
    }

    public static void saveProfile(String profileName, Profile profile) throws ProfileSaveException {
        saveProfile(getProfileFile(profileName), profile);
    }

    public static Profile loadDefaultProfile() {
        try {
            return loadProfile(getDefaultProfileFile());
        }
        catch (ProfileLoadException ex) {
            return new Profile();
        }
    }

    public static void saveDefaultProfile(Profile profile) throws ProfileSaveException {
        saveProfile(getDefaultProfileFile(), profile);
    }

    public static String validateProfileName(CharSequence profileName) throws BadProfileNameException
    {
        Matcher matcher = profileNameValidationPattern.matcher(profileName);
        if(matcher.find())
            return matcher.group(1);
        else
            throw new BadProfileNameException();
    }

    public static class BadProfileNameException extends Exception {
        public BadProfileNameException() {
            super("Invalid Profile Name");
        }
    }

    public static class ProfileIOException extends Exception {
        public ProfileIOException(Exception e) {
            super(e);
        }
    }

    public static class ProfileLoadException extends ProfileIOException {
        public ProfileLoadException(Exception e) {
            super(e);
        }
    }

    public static class ProfileSaveException extends ProfileIOException {
        public ProfileSaveException(Exception e) {
            super(e);
        }
    }

    public static class Profile extends HashMap<String, Integer> {
        Integer get(String key) {
            Integer i = super.get(key);
            return i==null?0:i;
        }
    }

    private static Profile loadProfile(InputStream profileXml) throws ProfileLoadException {
        try {
            Profile profile = new Profile();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(profileXml);
            NodeList items = document.getElementsByTagName("int");
            Element item;

            for(int i=0; i<items.getLength(); i++) {
                item=(Element) items.item(i);
                profile.put(item.getAttribute("name"), Integer.parseInt(item.getAttribute("value")));
            }

            return profile;
        } catch (ParserConfigurationException | IOException | SAXException ex) {
            throw new ProfileLoadException(ex);
        }
    }

    private static Profile loadProfile(File profileFile) throws ProfileLoadException {
        try {
            InputStream profileXml = new FileInputStream(profileFile);
            return loadProfile(profileXml);
        }
        catch (IOException ex) {
            throw new ProfileLoadException(ex);
        }
    }

    private static void saveProfile(OutputStream out, Profile profile) throws ProfileSaveException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.newDocument();
            Element xmlItem, map = document.createElement("map");
            document.appendChild(map);

            for(Map.Entry<String, Integer> item: profile.entrySet()) {
                xmlItem=document.createElement("int");
                xmlItem.setAttribute("name", item.getKey());
                xmlItem.setAttribute("value", item.getValue().toString());
                map.appendChild(xmlItem);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(out);

            transformer.transform(source, result);
        }
        catch (ParserConfigurationException | TransformerException ex) {
            throw new ProfileSaveException(ex);
        }
    }

    private static void saveProfile(File outFile, Profile profile) throws ProfileSaveException {
        try {
            OutputStream out = new FileOutputStream(outFile);
            saveProfile(out, profile);
        }
        catch (IOException ex) {
            throw new ProfileSaveException(ex);
        }
    }

    //TODO: If after 2026-04-07 then remove this and anything that depends on it
    @Deprecated
    public static boolean migratePre1dot2Profiles() {
        String oldProfilePath = Environment.getDataDirectory().getPath()+"/data/"+StaticContext.getAppContext().getPackageName()+"/shared_prefs/";
        String newProfilePath = ProfileManager.getProfilePath();
        File oldProfileDir = new File(oldProfilePath);
        File newProfileDir = new File(newProfilePath);
        File inFile;
        newProfileDir.mkdirs();
        int count=0;

        if(!oldProfileDir.isDirectory()) {
            return false;
        }

        try {
            inFile = new File(oldProfilePath + "default.xml");

            if(inFile.exists()) {
                saveDefaultProfile(loadProfile(new FileInputStream(inFile)));
                inFile.delete();
                count++;
            }

            for (String profileName : Util.safe_list(oldProfileDir)) {
                if(profileName.startsWith("profile-")) {
                    inFile = new File(oldProfilePath + profileName);
                    saveProfile(profileName.substring(8, profileName.length()-4), loadProfile(new FileInputStream(inFile)));
                    inFile.delete();
                    count++;
                }
            }
        }
        catch (IOException | ProfileIOException e) {
        }

        return count>0;
    }
}
