jsonFile= 'MixSignalFiltered.json';
%jsonFile= 'NoneBlinkDB.json';
%jsonFile= 'LongBlinkDB.json';
%jsonFile= 'ShortBlinkDB.json';

originalSignal = jsondecode(fileread(jsonFile)).data(:,2);
processedSignal = (originalSignal);

posibliBlink = 0;
posibliBlinkPosition = 0;

for i = 1:15:51000  
 if(processedSignal(i) < 830 && posibliBlink ==0)      
        disp('Existe un probable parpadeo')
        posibliBlink = 1;
        posibliBlinkPosition = i;    
     end
    
     if(posibliBlink == 1 && (posibliBlinkPosition+510) == i)
        disp('Se evalua el parpadeo despues de 2 segundos de una probabilidad de parpadeo')
        posibliBlink = 0;
        sampleToEvaluate = getSampleRange(processedSignal,i);
        knnFunction(sampleToEvaluate)
     end
     
 plot(getSampleRange(processedSignal,i));drawnow        
    
end
 
%{
for i = 1:51000  
     if(processedSignal(i) < -0.25 && posibliBlink ==0)      
        disp('Existe un probable parpadeo')
        posibliBlink = 1;
        posibliBlinkPosition = i;    
     end
    
     if(posibliBlink == 1 && (posibliBlinkPosition+510) == i)
        disp('Se evalua el parpadeo despues de 2 segundos de una probabilidad de parpadeo')
        posibliBlink = 0;
        sampleToEvaluate = getSampleRange(processedSignal,i);
        knnFunction(sampleToEvaluate)
     end

 plot(getSampleRange(processedSignal,i));drawnow        
    
 end
%}
%{
for i = 1:51000  
     if(mod(i,510) == 0)
        sampleToEvaluate = getSampleRange(processedSignal,i);
        knnValue =knnFunction(sampleToEvaluate);
    end

 plot(getSampleRange(processedSignal,i));drawnow        
    
 end
%}

