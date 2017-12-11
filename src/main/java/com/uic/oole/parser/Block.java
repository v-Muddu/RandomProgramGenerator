package com.uic.oole.parser;

import java.util.*;

public class Block implements Scope{
    private Map<String, Symbol> locals = new HashMap<String, Symbol>();
    private Map<String, Symbol> initializedVariables = new HashMap<String, Symbol>();
    private Scope enclosingScope;
    private String scopeName = "local";

    public Block(Scope enclosingScope){
        this.enclosingScope = enclosingScope;
    }

    @Override public String getScopeName(){
        return scopeName;
    }

    /** Where to look next for symbols;  */
    @Override public Scope getEnclosingScope(){
        return enclosingScope;
    }

    /** Define a symbol in the current scope */
    @Override public void define(Symbol sym){
        locals.put(sym.getName(), sym);
    }

    @Override public void initialize(Symbol sym){
        initializedVariables.put(sym.getName(), sym);
    }

    @Override public Symbol lookup(String name){
        if(locals.containsKey(name)){
            return locals.get(name);
        }else{
            return this.getEnclosingScope().lookup(name);
        }
    }

    @Override public Symbol lookupLocally(String name){
        return locals.get(name);
    }

    @Override public boolean hasBeenInitialized(String name){
        if(initializedVariables.containsKey(name)){
            return true;
        }else{
            return this.getEnclosingScope().hasBeenInitialized(name);
        }
    }

    @Override public Set<Symbol> getInitializedVariables(){
        return new HashSet<Symbol>(this.initializedVariables.values());
    }
}