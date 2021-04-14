# -*- coding: utf-8 -*-
"""
Created on Tue Jan 26 20:26:21 2021

@author: jessi
"""
# https://spacy.io/usage/spacy-101
import ujson
import spacy
import en_core_web_sm #model
from spacy.lang.en.stop_words import STOP_WORDS #MOD STOP WORDS LIST

#Pipeline:
    #Tokenization
    #Part of speech tagging
    #dependancy parsing (collect dependancy per word)
    #Stemming
    #stop words removal
    #frequent nouns
    #arrange as subject, verb, noun
    
def generateTitle(text):
    #Need to load the model. In this case the model is en_core_web_sm
    nlp = en_core_web_sm.load()
       
    #pipeline (tokenize, part of speech, dependency, CUSTOM)
    # Stop word list
    stop_list = ["'s", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
                 "Saturday", "Sunday", "January", "Februrary", "March", "April",
                 "May", "June", "July", "August", "September", "October", 
                 "November", "December", "2020", "2019", "EDT", "PST", "PDT", 
                 "am", "pm", "a.m.", "p.m.", "invite", "join", "|", "event",]
    
    #Update default stop word list (if needed)
    nlp.Defaults.stop_words.update(stop_list)
    for word in STOP_WORDS:
       lexeme = nlp.vocab[word]
       lexeme.is_stop = True
    doc = nlp(text)    
    
    #check out text (manual debug)
    #for token in doc:
    #    print(token.text, token.lemma_, token.pos_, token.tag_, token.dep_,
    #            token.shape_, token.is_alpha, token.is_stop)
        
        
    #Find the weight by count of dependancies
    #some python array
    dependancyDictionary = {}
    for token in doc:
        if (token.is_stop == False):
            if(dependancyDictionary.get(token) == None):
                dependancyDictionary[token] = [child for child in token.children]
    #print(dependancyDictionary) (manual debug)
        
    #Create object holding dependency types related to subject, verb, noun
    dependencyTypes = {"subject": ["nsubjpass", "nsubj", "Normal subject(passive)"],
                       "noun": ["NOUN", "pronoun","proper noun", "PRON", "PROPN", "NNP", "NN", "PRP", "pobj","compound"],
                       "verb": ["VERB", "VBN", "vbp", "verb", "past principle", "verb", "non 3rd person singular present", "VBP"]}    
    
    #print(dependencyTypes) (manual debug)
    
    #Find the words that have the most weight
    #Aproach: Has the most dependancies?    
    #iterate through dependancyDictionary and pull the top 
    #subject, noun, verb
    subject = []
    verb = []
    noun = []
    sLen = 0
    nLen = 0
    vLen = 0
    for dependancy in dependancyDictionary:
        #need to pull the token and inspect the attributes and get the lenth of dependancies
        for token in doc:
            if(dependancy == token):  
                #print(dependancy, token) (manual debug)
                #Subject check
                if (token.pos_ in dependencyTypes['subject'] 
                    or token.tag_ in dependencyTypes['subject']
                    or token.dep_ in dependencyTypes['subject']):
                    
                    #Length check
                    depLength = dependancyDictionary[dependancy]
                    if(len(depLength) > sLen):
                        sLen = len(depLength )              
                        subject.append(token.text)
                    
                #noun check
                if (token.pos_ in dependencyTypes['noun'] 
                    or token.tag_ in dependencyTypes['noun']
                    or token.dep_ in dependencyTypes['noun']):
                    
                    #Length check
                    depLength = dependancyDictionary[dependancy]
                    if(len(depLength) > nLen):
                        nLen = len(depLength ) 
                        noun.append(token.text)
                    
                #verb check
                if (token.pos_ in dependencyTypes['verb'] 
                    or token.tag_ in dependencyTypes['verb']
                    or token.dep_ in dependencyTypes['verb']):
                    verb.append(token.text)
                    
                    #Length check
                    depLength = dependancyDictionary[dependancy]
                    if(len(depLength) > vLen):
                        vLen = len(depLength )
                        verb.append(token.text)
    #print(subject) (manual debug)
    #print(verb)
    #print(noun)
    #print(dependancyDictionary)
    
    # #Noun chunks
    # print("======Noun chunks======")
    # for chunk in doc.noun_chunks:
    #     print(chunk.text, chunk.root.text,chunk.root.dep_,
    #             chunk.root.head.text)
      
    # print("======entity rec=======")          
    # #Entity recognition
    # for ent in doc.ents:
    #     print(ent.text, ent.label_)  
        
    #output (formatted title subject(Pn), verb, noun)
    #Check if each array (subject, noun, verb) have values.
    #if subject is empty use entity recognition?
    #arrange
    title = ""
    
    if (len(verb) > 0):
        title = title + verb[0]
    if (len(subject) > 0):
        title = title + " " + subject[0]
    if (len(noun) > 0):
        title = title + " " + noun[0]
    #print("=======my title======")    
    #print(title)
    
    #Noun chunks
    chunkTitle = ""
    chunkTitleRoot = ""
    chunkTitleDobj = ""
    chunkTitleNsubj = ""
    #print("======My title noun chunks======")
    for chunk in doc.noun_chunks:
        if (chunk.root.dep_ == "ROOT"):
            chunkTitleRoot = chunk.text
    
        elif (chunk.root.dep_ == "dobj"):
            chunkTitleDobj = chunk.root.head.text + " " + chunk.text
    
        elif (chunk.root.dep_ == "nsubj"):
            chunkTitleNsubj = chunk.text + " " + chunk.root.head.text
    if (chunkTitleRoot != ""):
        chunkTitle = chunkTitleRoot
    
    elif (chunkTitleDobj != ""):
        chunkTitle = chunkTitleDobj
    
    elif (chunkTitleNsubj != ""):
        chunkTitle = chunkTitleNsubj     
    
    #print(chunkTitle)
    
    #Create a JSON return object
    #{"Primary": "titleA", "Secondary": "titleB"}
    if (len(text) > 94):
        js = {
            "Primary": title,
            "Secondary": chunkTitle
            }
        return ujson.dumps(js)
    else:
        js = {
            "Primary": chunkTitle,
            "Secondary": title
            }
        return ujson.dumps(js)

#=============================================================================
# event_doc = "You are invited to the virtual wedding of Stephanie Long and Jarret Mccommack September 19th at 2pm in the afternoon"

# myjsObj = generateTitle(event_doc)
# print(myjsObj)
#=============================================================================
