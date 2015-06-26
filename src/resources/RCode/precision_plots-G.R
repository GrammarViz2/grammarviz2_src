require(ggplot2)
require(Cairo)
require(reshape)
require(scales)
require(RColorBrewer)
require(grid)
library(gridExtra)
require("lattice")

data=read.csv(file="accuracy.csv",header=T,as.is=T)

df=data.frame("SAX-VSM-G"=data$SAX.VSM.G,"SAX-VSM"=data$SAX.VSM)

sum(data$SAX.VSM.G)
sum(data$SAX.VSM)

px=c(df$SAX.VSM.G+0.015)
py=c(df$SAX.VSM+0.015)


df=data.frame(x=data$Euclidean.1NN,y=data$SAX.VSM,px,py,labels=paste(c(1:length(data$Euclidean.1NN))))

p <- ggplot(df, aes(x, y,label=labels)) + geom_point(size=4,col="coral3") + theme_bw() +
  geom_abline(intercept = 0, slope=1) + geom_text(aes(px,py,label=labels)) +
  geom_text(x = 0.17, y = 0.38, label = "SAX-VSM-G wins",col="cornflowerblue",size = 8,face="bold") +
  geom_text(x = 0.4, y = 0.05, label = "SAX-VSM wins",col="cornflowerblue",size = 8) +
  scale_x_continuous("SAX-VSM-G error", limits=c(0,0.6), breaks=seq(0,1,0.1)) + 
  scale_y_continuous("SAX-VSM error", limits=c(0,0.6), breaks=seq(0,1,0.1)) + 
  ggtitle("SAX-VSM-G and SAX-VSM accuracy comparison") +
  geom_abline(intercept = 0, slope=1) +
  theme(plot.title=element_text(size=18),axis.text.x=element_text(size=15), 
        axis.text.y=element_text(size=15),axis.title.x=element_text(size=18), 
        axis.title.y=element_text(size=18))
p

Cairo(width = 550, height = 550, file="comparison_sax-vsm-g-sax-vsm.png", type="png", pointsize=12, 
      bg = "transparent", canvas = "white", units = "px", dpi = 96)
print(p)
dev.off()
#
#
#
px=c(data$DTW.1NN+0.015)
py=c(data$SAX.VSM+0.015)

data=data.frame(x=data$DTW.1NN,y=data$SAX.VSM,px,py,labels=paste(c(1:length(data$DTW.1NN))))

p1 <- ggplot(data, aes(x, y,label=labels)) + geom_point(size=4,col="coral3") + theme_bw() +
  geom_abline(intercept = 0, slope=1) + geom_text(aes(px,py,label=labels)) +
  geom_text(x = 0.17, y = 0.42, label = "DTW 1-NN wins",col="cornflowerblue",size = 8,face="bold") +
  geom_text(x = 0.4, y = 0.05, label = "SAX-VSM wins",col="cornflowerblue",size = 8) +
  scale_x_continuous("DTW error", limits=c(0,0.6), breaks=seq(0,1,0.1)) + 
  scale_y_continuous("SAX-VSM error", limits=c(0,0.6), breaks=seq(0,1,0.1)) + 
  ggtitle("DTW 1-NN and SAX-VSM accuracy comparison") +
  geom_abline(intercept = 0, slope=1) +
  theme(plot.title=element_text(size=18),axis.text.x=element_text(size=15), 
        axis.text.y=element_text(size=15),axis.title.x=element_text(size=18), 
        axis.title.y=element_text(size=18))
p1

Cairo(width = 550, height = 550, file="comparison_dtw.png", type="png", pointsize=12, 
      bg = "transparent", canvas = "white", units = "px", dpi = 96)
print(p1)
dev.off()

Cairo(width = 1100, height = 500, file="comparison.png", type="png", pointsize=7, 
      bg = "transparent", canvas = "white", units = "px", dpi = 96)
print(arrangeGrob(p, p1, ncol=2))
dev.off()
