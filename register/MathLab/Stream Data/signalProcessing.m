function processedSignal = signalProcessing(originalSignal)

meanSignal = 850;
maxSignal = 155;

processedSignal = originalSignal - meanSignal;
processedSignal = (processedSignal) /(maxSignal);

end