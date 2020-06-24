package com.net128.oss.web.lib.filemanager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ParentInfoUtil {
    public static void main(String [] args) {
        System.out.println(getParentInfo(".").stream().map(ParentInfo::toString )
                .collect(Collectors.joining("\n")));
    }

    public static List<ParentInfo> getParentInfo(String path) {
        List<ParentInfo> parents=new ArrayList<>();
        File file;
        try {
            file = new File(path).getCanonicalFile();
        } catch (IOException e) {
            file = new File(path).getAbsoluteFile();
        }
        file = file.getParentFile();
        while(file != null) {
            parents.add(new ParentInfo(file.getAbsolutePath(), file.getName()));
            file = file.getParentFile();
        }
        File[] roots = File.listRoots();
        if(parents.size()>0) {
            ParentInfo pi = parents.get(parents.size() - 1);
            if("".equals(pi.name)) {
                if(roots.length>1)
                    pi.name = pi.path.replaceAll(":.*", "");
                else pi.name = ">";
            }
        }
        if(roots.length>1) {
            parents.add(new ParentInfo("/", ">"));
            System.out.println(Arrays.asList(roots).stream().map(File::toString )
                    .collect(Collectors.joining(", ")));
        }

        return parents;
    }

    public static class ParentInfo {
        public String path;
        public String name;
        public ParentInfo(String path, String name) {
            this.path = path;
            this.name = name;
        }

        public ParentInfo(){}

        @Override
        public String toString() {
            return path + " -> "+ name;
        }
    }
}
