/* Set of virtual nodes */
set VNodes;

/* Set of virtual links */
set VEdges, dimen 2;

/* Virtual Network lifetime */
param duration;

/* Security demanded by VNodes */
param secDemNode{vnode in VNodes};

/* Security demanded by VEdges */
param secDemEdge{(i,j) in VEdges};

/* CPU demanded by VNodes */
param cpuDem{vnode in VNodes};

/* Bw demanded by VEdges */
param bwDem{(i,j) in VEdges};

/* Cloud security demanded by VNodes */
param cloudSecDem{vnode in VNodes};

/* Tells if vnode need backup or not */
param wantBackup;



/* Set of substrate nodes */
set SNodes;

/* Set of substrate links */
set SEdges, dimen 2;

/* Weight depending if sedges is intra or inter domain */
param weight{u in SNodes, v in SNodes}, >= 0;

/* Security level provided by SNodes */
param secSupNode{snode in SNodes};

/* Security level provided by SEdges */
param secSupEdge{u in SNodes, v in SNodes};

/* CPU supplied by SNodes */
param cpuSup{snode in SNodes};

/* Bw supplied by SEdges */
param bwSup{u in SNodes, v in SNodes};

/* Cloud security provided by SNodes */
param cloudSecSup{snode in SNodes};

/* Substrate and virtual nodes */
set AllNodes := SNodes union VNodes;



/* Set of clouds */
set Clouds;

/* Tells if a substrate node snode belongs to cloud c */
param doesItBelong{c in Clouds, snode in SNodes};

/* Tells if the backup of vnode has to be in the same cloud or in other one */
param bvNodeLocalization{vnode in VNodes};

# ---------------VARS---------------------- #

/* Working flow variable */
var fw{i in VNodes, j in VNodes ,u in AllNodes, v in AllNodes}, >= 0;

/* Backup flow variable */
var fb{i in VNodes, j in VNodes ,u in AllNodes, v in AllNodes}, >= 0;

/* Binary indicator for fw */
var phiw{i in VNodes, j in VNodes,u in AllNodes, v in AllNodes}, binary;

/* Binary indicator for fb */
var phib{i in VNodes, j in VNodes,u in AllNodes, v in AllNodes}, binary;

/* Binary indicator for working nodes mapping */
var thetaw{VNodes,SNodes}, >= 0, binary;

/* Binary indicator for backup nodes mapping */
var thetab{VNodes,SNodes}, >= 0, binary;

/* Binary variable that indicates if a virtual node is in that cloud or not */
var cw{VNodes, Clouds}, >=0, binary;

/* Binary variable that indicates if a virtual node's backup is in that cloud or not */
var cb{VNodes, Clouds}, >=0, binary;

/* Reserved bw in sedge for backup */
var r{u in SNodes, v in SNodes}, >= 0; #SEdges ou u in SNodes, v in SNodes

/* Reserved CPU in snode for backup */
var gama{SNodes}, >= 0;

/* Aux vars */
var working{SNodes}, >= 0, binary;
var backup{SNodes}, >= 0, binary;

# -----------------OBJ--------------------- #

#minimize cost: sum{(i,j) in VEdges} sum{u in SNodes, v in SNodes} weight[u,v] * fw[i,j,u,v] + sum{u in SNodes, v in SNodes} r[u,v] + sum{p in VNodes} sum{w in SNodes} cpuSup[w] * thetaw[p,w] + sum{w in SNodes} gama[w];

minimize cost: sum{(i,j) in VEdges} sum{u in SNodes, v in SNodes} weight[u,v] * fw[i,j,u,v] * secSupEdge[u,v] + sum{u in SNodes, v in SNodes} r[u,v] * secSupEdge[u,v] + sum{p in VNodes} sum{w in SNodes} cpuSup[w] * thetaw[p,w] * secSupNode[w] * cloudSecSup[w] + sum{w in SNodes} gama[w] * secSupNode[w] * cloudSecSup[w] + sum{(i,j) in VEdges} sum{u in SNodes, v in SNodes} phiw[i,j,u,v] + sum{(i,j) in VEdges} sum{u in SNodes, v in SNodes} phib[i,j,u,v];

# -----------------CONST------------------- #

/* Working flow conservation */
s.t. fwConst0{(i,j) in VEdges, v in SNodes}: sum{u in AllNodes} fw[i,j,u,v] - sum{u in AllNodes} fw[i,j,v,u] = 0;
s.t. fwConst1{(i,j) in VEdges}: sum{v in SNodes} fw[i,j,i,v] - sum{v in SNodes} fw[i,j,v,i] = bwDem[i,j];
s.t. fwConst2{(i,j) in VEdges}: sum{v in SNodes} fw[i,j,j,v] - sum{v in SNodes} fw[i,j,v,j] = -bwDem[i,j];
s.t. fwConst3{(i,j) in VEdges, v in SNodes}: thetaw[i,v] * bwDem[i,j] = fw[i,j,i,v];
s.t. fwConst4{(i,j) in VEdges, v in SNodes}: thetaw[j,v] * bwDem[i,j] = fw[i,j,v,j];

/* Backup flow conservation */
s.t. fbConst0{(i,j) in VEdges, v in SNodes}: sum{u in AllNodes} fb[i,j,u,v] - sum{u in AllNodes} fb[i,j,v,u] = 0;
s.t. fbConst1{(i,j) in VEdges}: sum{v in SNodes} fb[i,j,i,v] - sum{v in SNodes} fb[i,j,v,i] = bwDem[i,j] * wantBackup;
s.t. fbConst2{(i,j) in VEdges}: sum{v in SNodes} fb[i,j,j,v] - sum{v in SNodes} fb[i,j,v,j] = -bwDem[i,j] * wantBackup;
s.t. fbConst3{(i,j) in VEdges, v in SNodes}: thetab[i,v] * bwDem[i,j] = fb[i,j,i,v] * wantBackup;
s.t. fbConst4{(i,j) in VEdges, v in SNodes}: thetab[j,v] * bwDem[i,j] = fb[i,j,v,j] * wantBackup;

/* Capacity links constraints */
s.t. capConst0{u in SNodes, v in SNodes}: sum{(i,j) in VEdges} (fb[i,j,u,v] + fb[i,j,v,u]) <= r[u,v];
s.t. capConst1{u in SNodes, v in SNodes}: sum{(i,j) in VEdges} (fw[i,j,u,v] + fw[i,j,v,u]) + r[u,v] <= bwSup[u,v];

/* Nodes capacity constraints */
s.t. nodeCapConst0{v in SNodes}: sum{u in VNodes} thetab[u,v] * cpuDem[u] <= gama[v];
s.t. nodeCapConst1{v in SNodes}: sum{u in VNodes} thetaw[u,v] * cpuDem[u] + gama[v] <= cpuSup[v];

/* Virtual node mapping */
s.t. nodeMapConst0{u in VNodes}: sum{v in SNodes} thetaw[u,v] = 1;
s.t. nodeMapConst1{u in VNodes}: sum{v in SNodes} thetab[u,v] = wantBackup;
s.t. nodeMapConst2{v in SNodes, z in VNodes}: sum{u in VNodes} thetaw[u,v] + thetab[z,v] <= 1;
s.t. nodeMapConst3{v in SNodes, z in VNodes}: sum{u in VNodes diff {z}} thetab[u,v] + thetab[z,v] <= 1;

/* Constraints related to clouds */
s.t. nodeLocConst0{u in VNodes}: sum{k in Clouds} cw[u,k] = 1;
s.t. nodeLocConst1{u in VNodes}: sum{k in Clouds} cb[u,k] = wantBackup;
s.t. nodeLocConst2{u in VNodes, k in Clouds}: wantBackup * cw[u,k] + cb[u,k] <= bvNodeLocalization[u];
s.t. nodeLocConst3{u in VNodes, k in Clouds}: cw[u,k] >= cb[u,k] * bvNodeLocalization[u] - 1;

s.t. nodeLocConst4{u in VNodes,  k in Clouds}: sum{v in SNodes} (thetaw[u,v] * doesItBelong[k,v]) >= cw[u,k];
s.t. nodeLocConst5{u in VNodes,  k in Clouds}: sum{v in SNodes} (thetab[u,v] * doesItBelong[k,v]) >= cb[u,k];

s.t. cloudConst0{u in VNodes, v in SNodes}: thetaw[u,v] * cloudSecDem[u] <= cloudSecSup[v];
s.t. cloudConst1{u in VNodes, v in SNodes}: thetab[u,v] * cloudSecDem[u] <= cloudSecSup[v];

s.t. flowConst{i in VNodes, v in SNodes}: sum{j in VNodes diff {i}, k in VNodes diff {i}} (fw[j,k,i,v] + fw[j,k, v,i] + fb[j,k,i,v] + fb[j,k,v,i]) = 0;

/* Constraints to relate variables */
s.t. relConst0{(i,j) in VEdges, v in SNodes}: thetaw[i,v] * bwDem[i,j] >= phiw[i,j,i,v];
s.t. relConst1{(i,j) in VEdges, v in SNodes}: thetaw[j,v] * bwDem[i,j] >= phiw[i,j,v,j];
s.t. relConst2{(i,j) in VEdges, u in AllNodes, v in AllNodes}: bwDem[i,j] * phiw[i,j,u,v] >= fw[i,j,u,v];

s.t. relConst3{(i,j) in VEdges, v in SNodes}: thetab[i,v] * bwDem[i,j] >= phib[i,j,i,v];
s.t. relConst4{(i,j) in VEdges, v in SNodes}: thetab[j,v] * bwDem[i,j] >= phib[i,j,v,j];
s.t. relConst5{(i,j) in VEdges, u in AllNodes, v in AllNodes}: bwDem[i,j] * phib[i,j,u,v] >= fb[i,j,u,v];

/* Node Security Constraints */
s.t. nodeSecConst0{u in VNodes, v in SNodes}: thetaw[u,v] * secDemNode[u] <= secSupNode[v];
s.t. nodeSecConst1{u in VNodes, v in SNodes}: thetab[u,v] * secDemNode[u] <= secSupNode[v];

/* Link Security Constraints */
s.t. linkSecConst0{(i,j) in VEdges, u in SNodes, v in SNodes}: phiw[i,j,u,v] * secDemEdge[i,j] <= secSupEdge[u,v];
s.t. linkSecConst1{(i,j) in VEdges, u in SNodes, v in SNodes}: phib[i,j,u,v] * secDemEdge[i,j] <= secSupEdge[u,v];

/* Binary Constraints */
s.t. binConst0{(i,j) in VEdges, u in AllNodes, v in AllNodes}: phiw[i,j,u,v] = phiw[i,j,v,u];
s.t. binConst1{(i,j) in VEdges, u in AllNodes, v in AllNodes}: phib[i,j,u,v] = phib[i,j,v,u];

/* Links and Nodes Disjointness*/
#s.t. disjConst0{(i,j) in VEdges, u in SNodes, v in SNodes}: thetab[i,u] + phiw[i,j,u,v] <= 1;
#s.t. disjConst1{(i,j) in VEdges, u in SNodes, v in SNodes}: thetab[j,u] + phiw[i,j,u,v] <= 1;
#s.t. disjConst2{(i,j) in VEdges, u in SNodes, v in SNodes}: thetaw[i,u] + phib[i,j,u,v] <= 1;
#s.t. disjConst3{(i,j) in VEdges, u in SNodes, v in SNodes}: thetaw[j,u] + phib[i,j,u,v] <= 1;

s.t. aux0{(i,j) in VEdges, u in SNodes}: 1000 * working[u] >= sum{v in SNodes} phiw[i,j,u,v];
s.t. aux1{(i,j) in VEdges, u in SNodes}: 1000 * backup[u] >= sum{v in SNodes} phib[i,j,u,v];
s.t. aux2{u in SNodes}: backup[u] = 1 - working[u];

solve;

printf "Function cost: %f\n", cost;

printf{(i,j) in VEdges} "Sum of all phiw to %d and %d: %d\n", i,j, sum{u in SNodes, v in SNodes} phiw[i,j,u,v];
printf{(i,j) in VEdges} "Sum of all phib to %d and %d: %d\n", i,j, sum{u in SNodes, v in SNodes} phib[i,j,u,v]; 

printf{(i,j) in VEdges, u in AllNodes, v in AllNodes} "Variable fw[%d,%d,%s,%s]: %f\n", i,j,u,v,fw[i,j,u,v];
printf{(i,j) in VEdges, u in AllNodes, v in AllNodes} "Variable fb[%d,%d,%s,%s]: %f\n", i,j,u,v,fb[i,j,u,v];

printf{(i,j) in VEdges, u in AllNodes, v in AllNodes} "Variable phiw[%d,%d,%s,%s]: %d\n", i,j,u,v,phiw[i,j,u,v];
printf{(i,j) in VEdges, u in AllNodes, v in AllNodes} "Variable phib[%d,%d,%s,%s]: %d\n", i,j,u,v,phib[i,j,u,v];

printf{i in VNodes, j in SNodes} "Variable thetaw[%d,%s]: %d\n", i,j,thetaw[i,j];
printf{i in VNodes, j in SNodes} "Variable thetab[%d,%s]: %d\n", i,j,thetab[i,j];

printf{k in Clouds, v in VNodes} "Variable cw[%d,%d]: %d\n", v,k,cw[v,k];
printf{k in Clouds, v in VNodes} "Variable cb[%d,%d]: %d\n", v,k,cb[v,k];

printf{i in SNodes, j in SNodes} "Variable r[%s,%s]: %f\n", i,j,r[i,j];

printf{u in VNodes, k in Clouds} "Variable cw[%d,%d] + cb[%d,%d]: %d\n", u,k,u,k,cw[u,k] + cb[u,k];

printf{i in SNodes} "Variable gama[%s]: %f\n", i,gama[i];

end;
