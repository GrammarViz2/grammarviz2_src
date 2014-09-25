require(ggplot2)
require(Cairo)
require(reshape)
require(scales)
require(RColorBrewer)
require(grid)
library(gridExtra)
require(lattice)
require(rgl)
require(akima)
require(DiceKriging)

# ECG 0606
#
dat = read.table("TEK16_tmp.txt",sep=",",header=F,as.is=T)
names(dat)=c("winSize", "paaSize", "aSize", "meanCoverage", "maxObservedCoverage", 
             "minObservedCoverage", "approximationDistance","grammarSize","totalZeroes");

xyz <- with(dat, interp(x=grammarSize,y=approximationDistance,z=totalZeroes, 
                        xo=sort(unique(grammarSize)), duplicate="mean", extrap=FALSE ))
with(xyz, persp3d(x,y,z, col=heat.colors(length(z))[rank(z)], xlab='grammarSize', 
                  ylab='approximationDistance', zlab='totalZeroes', main=''))



unique(dat$winSize)

slice=dat[dat$winSize==180,]

p=wireframe(approximationDistance ~ paaSize * aSize, data = slice, 
            scales = list(arrows = FALSE),
            drape = TRUE, colorkey = TRUE), screen = list(z = 210, x = -66, y=20),
            aspect = c(97/77, 0.8),
            xlim=range(dat$grammarSize), ylim=range(dat$approximationDistance), 
            zlim=c(0, max(dat$minObservedCoverage)+10),
            main=paste("Min coverage plot"),
            col.regions = terrain.colors(100, alpha = 1) )
p

xyz <- with(dat, interp(x=grammarSize,y=meanCoverage,z=totalZeroes, 
                        xo=sort(unique(grammarSize)), duplicate="mean", extrap=FALSE ))
with(xyz, persp3d(x,y,z, col=heat.colors(length(z))[rank(z)], xlab='grammarSize', 
                  ylab='meanCoverage', zlab='totalZeroes', main=''))


unique(dat$winSize)
slice=dat[dat$winSize==700,]
xyz <- with(dat, interp(x=grammarSize,y=meanCoverage,z=totalZeroes, 
                          xo=sort(unique(grammarSize)), extrap=FALSE ))
with(xyz, persp3d(x,y,z, col=heat.colors(length(z))[rank(z)], xlab='grammarSize', 
                  ylab='meanCoverage', zlab='totalZeroes', main=''))





toSend = data.frame(x=dat$approximationDistance,y=dat$grammarSize,z=dat$totalZeroes)
write.table(toSend, file="set1.table",col.names = TRUE,row.names=F,sep="\t")

toSend = data.frame(x=dat$approximationDistance,y=dat$grammarSize,z=dat$meanCoverage)
write.table(toSend, file="set2.table",col.names = TRUE,row.names=F,sep="\t")

p=ggplot(dat, aes(x=grammarSize,y=approximationDistance,fill=minObservedCoverage)) + geom_raster()
p

p=wireframe(minObservedCoverage ~ grammarSize * approximationDistance, data = dat, 
            scales = list(arrows = FALSE),
            drape = TRUE, colorkey = TRUE, screen = list(z = 210, x = -66, y=20),
            aspect = c(97/77, 0.8),
            xlim=range(dat$grammarSize), ylim=range(dat$approximationDistance), 
            zlim=c(0, max(dat$minObservedCoverage)+10),
            main=paste("Min coverage plot"),
            col.regions = terrain.colors(100, alpha = 1) )
p

p=cloud(totalZeroes ~ grammarSize * approximationDistance, data = dat, 
            scales = list(arrows = FALSE),
            drape = TRUE, colorkey = TRUE)
p

p=cloud(z ~ x * y, data = xyz, 
        scales = list(arrows = FALSE),
        drape = TRUE, colorkey = TRUE)
p

str(dat)

slice=dat[(dat$approximationDistance>45 & dat$approximationDistance<46),]
xyz <- with(slice, interp(x=paaSize,y=aSize,z=approximationDistance, 
                        xo=sort(unique(paaSize)), extrap=FALSE ))
with(xyz, persp3d(x,y,z, col=heat.colors(length(z))[rank(z)], xlab='PAA', 
                  ylab='ALPHABET', zlab='Approximation Distance', main=''))



xyz <- with(dat, interp(x=dat$grammarSize,y=dat$paaSize,z=dat$totalZeroes, 
                         xo=sort(unique(dat$grammarSize)), extrap=FALSE ))
with(xyz, persp3d(x,y,z, col=heat.colors(length(z))[rank(z)], xlab='Grammar Size', 
                  ylab='Approx. distance', zlab='Total Zeroes', main=''))


xyz <- with(dat, interp(x=dat$grammarSize,y=dat$approximationDistance,z=dat$meanCoverage, 
                        yo=sort(unique(dat$approximationDistance)), 
            xo=seq(min(dat$grammarSize), max(dat$grammarSize), length = 10), extrap=FALSE ))

with(xyz, persp3d(x,y,z, col=heat.colors(length(z))[rank(z)], xlab='Grammar Size', 
                  ylab='Approx. distance', zlab='Mean Coverage', main=''))



persp(dat$grammarSize,dat$approximationDistance,dat$minObservedCoverage) 

Cairo(width = 700, height = 750, file="gun/parameters.png", type="png", pointsize=12, 
      bg = "white", canvas = "white", units = "px", dpi = "auto")
print(p)
dev.off()



xyz <- with(puts, interp(x=dat$grammarSize,y=dat$approximationDistance,z=dat$minObservedCoverage, 
                         xo=sort(unique(dat$minObservedCoverage)), extrap=FALSE ))

open3d()
rgl.surface(x=dat$grammarSize,y=dat$approximationDistance,z=dat$minObservedCoverage,
            coords=c(1,3,2))
axes3d()


library(rgl)
data(akima)
# data
# dat$grammarSize,dat$approximationDistance,dat$minObservedCoverage
rgl.spheres(dat$grammarSize,dat$minObservedCoverage , dat$approximationDistance,color="red")
rgl.bbox()


X = read.table('set2.table')

design <- data.frame(V1=as.numeric(X$V1), V2=as.numeric(X$V2))
response <- as.numeric(X$V3)

model <- km(~., design=design, response=response,lower=c(1,1), upper=c(100,100), covtype="matern3_2")

sectionview3d(model, xlim=c(min(design$V1),max(design$V1)), ylim=c(min(design$V2),max(design$V2)))

n.grid <- 51
x.grid <- seq(min(design$V1), max(design$V1),, n.grid)
y.grid <- seq(min(design$V2), max(design$V2),, n.grid)
test.grid <- expand.grid(x.grid, y.grid)
pred <- predict(model, newdata=test.grid, type="UK", checkNames=FALSE)
z.grid <- matrix(pred$mean, n.grid, n.grid)
filled.contour(x.grid, y.grid, z.grid, color = terrain.colors, nlevels = 50)
persp(x.grid, y.grid, z.grid)

library(rgl)
rgl.surface(x.grid, y.grid, z.grid)
