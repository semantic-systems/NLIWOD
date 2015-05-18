

"FATN_mit_ranking_1_50 - OneFeature.tsv"        
"FATN_mit_ranking_1_50 - TwoFeatures.tsv
"FATN_mit_ranking_1_50 - ThreeFeatures.tsv"     
"FATN_mit_ranking_1_50 - FourandfiveFeature.tsv"

if (!require("RColorBrewer")) {
	install.packages("RColorBrewer")
	library(RColorBrewer)
	display.brewer.all()
}

library(lattice)

myColours <- colorRampPalette(brewer.pal(9,"PuBu"))(15)
my.settings <- list(
  superpose.polygon=list(col=myColours[4:15], border="transparent"),
  strip.border=list(col="black")
)

par(cex.lab=2)
pdf("ranking_1_withoutpattern.pdf",21,6)
data <- read.csv("FATN_mit_ranking_1_50 - OneFeature.tsv",sep="\t",header=T)
barchart(F.measure~Feature, group=Nr..of.result.sets, data=data,horizontal=F, box.ratio=4, xlab="N",ylim=c(0,0.65), par.settings=my.settings, ylab="F-measure",auto.key=list(space="right", columns=1, cex.title=1))
dev.off()



pdf("ranking_2_withoutpattern.pdf",21,6)
data <- read.csv("FATN_mit_ranking_1_50 - TwoFeatures.tsv",sep="\t",header=T)
barchart(F.measure~Feature, group=Nr..of.result.sets, data=data,horizontal=F, box.ratio=4, xlab="N",ylim=c(0,0.65), par.settings=my.settings, ylab="F-measure",auto.key=list(space="right", columns=1, cex.title=1))
dev.off()







#######
par(cex.label=2)
pdf("ranking_1.pdf",10,12)
data <- read.csv("FATN_mit_ranking_1_50 - OneFeature.tsv",sep="\t",header=T)
barchart(Feature~F.measure, data=data, groups=Nr..of.result.sets, horizontal=T, box.ratio=4, xlab="Micro F-measure", par.settings = my.settings,auto.key=list(space="top", columns=5, cex.title=1))
dev.off()

pdf("ranking_2.pdf",10,12)
data <- read.csv("FATN_mit_ranking_1_50 - TwoFeatures.tsv",sep="\t",header=T)
barchart(Feature~F.measure, data=data, groups=Nr..of.result.sets, horizontal=T, box.ratio=4, xlab="Micro F-measure", par.settings = my.settings,auto.key=list(space="top", columns=5, cex.title=1))
dev.off()

pdf("ranking_3.pdf",10,12)
data <- read.csv("FATN_mit_ranking_1_50 - ThreeFeatures.tsv",sep="\t",header=T)
barchart(Feature~F.measure, data=data, groups=Nr..of.result.sets, horizontal=T, box.ratio=4, xlab="Micro F-measure", par.settings = my.settings,auto.key=list(space="top", columns=5, cex.title=1))
dev.off()

pdf("ranking_45.pdf",10,12)
data <- read.csv("FATN_mit_ranking_1_50 - FourandfiveFeature.tsv",sep="\t",header=T)
barchart(Feature~F.measure, data=data, groups=Nr..of.result.sets, horizontal=T, box.ratio=4, xlab="Micro F-measure", par.settings = my.settings,auto.key=list(space="top", columns=5, cex.title=1))
dev.off()
