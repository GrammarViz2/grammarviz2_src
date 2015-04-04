require(ggplot2)
require(gridExtra)
require(plyr)
require(reshape)
require(Cairo)

# ECG 0606
#
dat = read.table("../ecg0606_res.txt",sep=",",header=T,as.is=T)
range(dat$winSize)
range(dat$paaSize)
range(dat$aSize)
cfun=function(x){
  if(1==x[1] && 0==x[2]){
    "hit"
  }else if(1==x[1] && 1==x[2]){
    "ambiguous"
  }else{
  "miss"
  }
}

dd=data.frame(dat$winSize,dat$paaSize,dat$aSize,
              approximationDistance=dat$approximationDistance,
              grammarSize=dat$grammarSize,
              densityBasedDiscord=apply(cbind(dat$r3,dat$r4),1,cfun))
dim(dd[dd$densityBasedDiscord=="hit",])
range(dd[dd$densityBasedDiscord=="hit",1])
range(dd[dd$densityBasedDiscord=="hit",2])
range(dd[dd$densityBasedDiscord=="hit",3])

dd=data.frame(dat$winSize,dat$paaSize,dat$aSize,
              approximationDistance=dat$approximationDistance,
              grammarSize=dat$grammarSize,
              dat$r5)
dim(dd[dd$dat.r5==1,])
range(dd[dd$dat.r5==1,1])
range(dd[dd$dat.r5==1,2])
range(dd[dd$dat.r5==1,3])


dd=data.frame(approximationDistance=dat$approximationDistance,
              grammarSize=dat$grammarSize,
              densityBasedDiscord=apply(cbind(dat$r3,dat$r4),1,cfun))
p=ggplot(dd,aes(approximationDistance,grammarSize,color=densityBasedDiscord,fill=densityBasedDiscord)) +
stat_density2d(aes(fill = densityBasedDiscord), alpha=0.3, geom=c("polygon","density2d"))+
theme_bw() + theme(legend.position="bottom") +
ggtitle("Rule-density-based anomaly discovery results")
p

p2=ggplot(dd[dd$densityBasedDiscord=="hit",],aes(approximationDistance,grammarSize)) + 
  #stat_density2d(alpha=0.8, color="cyan",geom="point",aes(size = ..density..),contour=F)+
  stat_density2d(aes(fill = densityBasedDiscord), alpha=0.3, color="brown3", fill="brown1", geom=c("polygon","density2d"))+
  scale_x_continuous("Approximation distance",limits=c(0,200)) +
  scale_y_continuous("Grammar size", limits=range(dd$grammarSize)) + 
  ggtitle("Area with successful rule density-based\n discovery of the true anomaly")+
  theme_bw() + 
  theme(legend.position="bottom",plot.title=element_text(size=20),
        axis.title.x=element_text(size=16), axis.title.y=element_text(size=16),
        axis.text.x=element_text(size=12),axis.text.y=element_text(size=12))
p2 

print(arrangeGrob(p,p2, ncol=2))

dhits=dat$r5
dhits[dhits==0]="miss"
dhits[dhits==1]="hit"
dd=data.frame(approximationDistance=dat$approximationDistance,grammarSize=dat$grammarSize,"RRA-discord"=dhits)

p3=ggplot(dd,aes(approximationDistance,grammarSize,color=RRA.discord,fill=RRA.discord)) +
  stat_density2d(aes(fill = RRA.discord), alpha=0.3, geom=c("polygon","density2d"))+
  theme_bw() + theme(legend.position="bottom") +
  ggtitle("RRA-based discord discovery results")
p3

p4=ggplot(dd[dd$RRA.discord=="hit",],aes(approximationDistance,grammarSize)) + 
  stat_density2d(aes(fill = densityBasedDiscord), alpha=0.3, color="cyan3", fill="cyan", geom=c("polygon","density2d"))+
  scale_x_continuous("Approximation distance",limits=c(0,200)) +
  scale_y_continuous("Grammar size", limits=range(dd$grammarSize)) + 
  ggtitle("Area with successful RRA-based\n discovery of the true anomaly")+
  theme_bw() + 
  theme(legend.position="bottom",plot.title=element_text(size=20),
        axis.title.x=element_text(size=16), axis.title.y=element_text(size=16),
        axis.text.x=element_text(size=12),axis.text.y=element_text(size=12))
p4

print(arrangeGrob(p2,p4, ncol=2))

print(arrangeGrob(p,p3, ncol=2))

Cairo(width = 1200, height = 700, 
      file="ecg0606_areas.pdf", 
      type="pdf", pointsize=12, 
      bg = "transparent", canvas = "white", units = "px", dpi = 82)
print(arrangeGrob(p2,p4, ncol=2))
dev.off()
