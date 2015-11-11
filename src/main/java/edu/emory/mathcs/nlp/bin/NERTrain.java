/**
 * Copyright 2015, Emory University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.emory.mathcs.nlp.bin;

import java.io.*;
import java.util.*;

import edu.emory.mathcs.nlp.common.util.BinUtils;
import edu.emory.mathcs.nlp.common.util.Splitter;
import edu.emory.mathcs.nlp.emorynlp.component.NLPOnlineComponent;
import edu.emory.mathcs.nlp.emorynlp.component.config.NLPConfig;
import edu.emory.mathcs.nlp.emorynlp.component.feature.FeatureTemplate;
import edu.emory.mathcs.nlp.emorynlp.component.node.NLPNode;
import edu.emory.mathcs.nlp.emorynlp.component.train.NLPOnlineTrain;
import edu.emory.mathcs.nlp.emorynlp.ner.NERConfig;
import edu.emory.mathcs.nlp.emorynlp.ner.NERState;
import edu.emory.mathcs.nlp.emorynlp.ner.NERTagger;
import edu.emory.mathcs.nlp.emorynlp.ner.features.NERFeatureTemplate0;
import edu.emory.mathcs.nlp.emorynlp.ner.AmbiguityClassMap;

/**
 * @author Jinho D. Choi ({@code jinho.choi@emory.edu})
 */
public class NERTrain extends NLPOnlineTrain<NLPNode,NERState<NLPNode>>
{
    static public HashMap<String, Set<String>> hm = new HashMap<>();

	public NERTrain(String[] args)
	{
		super(args);
	}
	
	@Override
	protected NLPOnlineComponent<NLPNode,NERState<NLPNode>> createComponent(InputStream config)
	{
		return new NERTagger<>(config);
	}

	@Override
	protected void initComponent(NLPOnlineComponent<NLPNode,NERState<NLPNode>> component, List<String> inputFiles) throws IOException {
		initComponentSingleModel(component, inputFiles);

        List<String> ambiDicts = new ArrayList<>();
        //ambiDicts.add("prefixtree_dbpedia_undigitalized.txt");
        ambiDicts.add("tmp.txt");
        createAmbiguityClasseMap(ambiDicts);
	}
	
	@Override
	protected FeatureTemplate<NLPNode,NERState<NLPNode>> createFeatureTemplate()
	{
		switch (feature_template)
		{
		case  0: return new NERFeatureTemplate0<NLPNode>();
		default: throw new IllegalArgumentException("Unknown feature template: "+feature_template);
		}
	}
	
	@Override
	protected NLPNode createNode()
	{
		return new NLPNode();
	}

    protected void createAmbiguityClasseMap(List<String> inputFiles) throws IOException {
        BufferedReader rdr = new BufferedReader(new FileReader(inputFiles.get(0)));
        String line;

        while((line = rdr.readLine()) != null){
            if(line.equals("")) continue;

            String[] tmp = Splitter.splitTabs(line);
            String[] tmp0 = Splitter.splitSpace(tmp[0]);
            String[] tmp1 = Splitter.splitSpace(tmp[1]);
            int len = tmp0.length;
            for (int i = 0; i < len; i ++){
                if (! hm.containsKey(tmp0[i].toLowerCase())) {
                    hm.put(tmp0[i].toLowerCase(), new HashSet<>());
                }
                String value;
                switch (len){
                    case (1): value = "U-";
                        break;
                    case (2): value = (i == 0)? "B-": "L-";
                        break;
                    default: value = (i == 0)? "B-" : (i == (len -1))? "L-" : "I-";
                        break;
                }
                for (String t: tmp1)
                    hm.get(tmp0[i].toLowerCase()).add(value + t);
            }
        }
        BinUtils.LOG.info(String.format("- # of ambiguity classes: %d\n", hm.size()));
    }

	static public void main(String[] args) throws IOException {
		new NERTrain(args).train();
	}
}
