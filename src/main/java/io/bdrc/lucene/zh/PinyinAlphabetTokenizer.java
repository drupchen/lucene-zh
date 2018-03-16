/*******************************************************************************
 * Copyright (c) 2018 Buddhist Digital Resource Center (BDRC)
 *
 * If this file is a derivation of another work the license header will appear
 * below; otherwise, this work is licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.
 *
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package io.bdrc.lucene.zh;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * Taken from {@link https://github.com/medcl/elasticsearch-analysis-pinyin}
 * and adapted to fit in PinyinTokenizer.
 * 
 * Takes the input string(a pinyin token) and tries to match a substring where: 
 *      - starting index: the end of the last found syllable
 *      - ending index: length of input string or less (decrements until a match is found)  
 * 
 * Created by medcl ({@link https://github.com/medcl}) on 16/10/13.
 * 
 */
public class PinyinAlphabetTokenizer {
        
        static List<Character> unihanPinyinDiacritics = Arrays.asList('ā', 'á', 'ǎ', 'à', 'ē', 'é', 'ě', 'è', 'ī', 'í', 'ǐ', 'ì', 
                'ō', 'ó', 'ǒ', 'ò', 'ū', 'ú', 'ǔ', 'ù', 'ǖ', 'ǘ', 'ǚ', 'ǜ', 'ǹ', '̀', 'ḿ', 'ń', 'ň');
    
        public static List<String> walk(String text) {
            int maxLength=6;
            text = text.toLowerCase();
            LinkedList<String> candidates=new LinkedList<>();
            StringBuffer buffer=new StringBuffer();
            boolean lastWord=true;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if ((c > 96 && c < 123) || (c > 64 && c < 91) || unihanPinyinDiacritics.contains(c)) {
                    if(!lastWord){
                        String str = buffer.toString();
                        buffer.setLength(0);
                        candidates.add(str);
                    }
                    buffer.append(c);
                    lastWord=true;
                }else{
                    //meet non letter
                    if(lastWord){
                        parse(candidates, buffer,true);
                        if(buffer.length()>0){
                            String str = buffer.toString();
                            buffer.setLength(0);
                            candidates.add(str);
                        }
                    }
                    buffer.append(c);
                    lastWord=false;
                }

                //start to check pinyin
                if(buffer.length()>=maxLength){
                    parse(candidates, buffer,false);
                }
            }

            //cleanup
            if(lastWord){
                parse(candidates,buffer,true);
            }

            //final cleanup
            if(buffer.length()>0){
                candidates.add(buffer.toString());
            }

            return candidates;
        }

    private static void parse(LinkedList<String> candidates, StringBuffer buffer,Boolean last) {
        for (int j = 0; j < buffer.length(); j++) {
            String guess=buffer.substring(0,buffer.length()-j);
            if(PinyinAlphabetDict.getInstance().match(guess)){
                candidates.add(guess);
                String left=buffer.substring(buffer.length()-j,buffer.length());
                buffer.setLength(0);
                buffer.append(left);
                if(!last){
                    break;
                }else{
                    if(left.length()>0){
                        parse(candidates,buffer,last);
                    }
                }
            }
        }
    }

}
