# **Zero-sum Cryptocurrency System: A New Approach to Design a Scalable Blockchain Protocol (TBD)**

Author: Tuyet Duong 

## 1, Abstract

This page introduces a Zero-sum blockchain, the first scalable blockchain that combines proof-of-work (PoW) and proof-of-stake (PoS) mechanisms in a secure and scalable way so that the blockchain system can achieve both security and scalability (the best of both PoW and PoS). Different from ordinary blockchain designs, this work proposes a novel approach to improve the throughput and stability of current blockchain protocols.

## 2, Introduction

### 2.1 Proof-of-work vs Proof-of-stake: 

PoW based blockchain like Bitcoin faces several major issues as follows

* PoW does consume a lot of energy
* PoW blockchains are not scalable in term of throughput. For example, Bitcoin on-chain tranasction processing of the bitcoin network is limited by the average block creation time of 10 minutes.
* With the popularity of cryptocurrency, the assumption of majority of honest mining power became very challenging. Some 51% attacks happen recently such as the attacks to the Ethereum Classic, Bitcoin Gold, Vertcoin...

Because of these issues, Proof-of-Stake was proposed to replace PoW. Several prominent PoS blockchains are Ouroboros, Snow White, and Algorand. However, without the uses of mining resources, these PoS based blockchains all suffer from costless simulation attacks such as long-range or nothing-at-stake attacks. There are many solutions to protect PoS protocols against these attacks. But people are still believe in PoW than PoS because of future unforseen security problems. Moreover, with PoS a small group of wealthy stakers can control the entire network. If and when that happens, a PoS system will be no different from a central banking fiat system.

Therefore, I suggest that pure PoS based systems should be only used for other applications than cryptocurrency, such as supply-chain managment, healthcase. Other applications that you can untilize the decentralization, immutability, traceability, or trackability of blockchain. Whereas for cryptocurrency purposes, a PoW-based or hybrid PoW-PoS is best suited.

### 2.2 2-hop/Twinscoin blockchain Pros and Cons: 
The 2-hop (Twinscoin) idea is to combine PoW and PoS to protect blockchain like Bitcoin in the presence of 51% attack. To successfully attack the blockchain systems, an adversary has to control the majority of resource including stake and computing power. Still, the throughput is as same as the throught of the PoW blockchain because each PoW is only assigned to a PoS.

## 3, Zero-sum Blockchain

## 3.1 Design Idea 

Instead of having two chains (Pow and PoS chains) produce blocks at the same rate as in the 2-hop design (one PoW block - on PoS block). I propose to let each chain grow at its own rate; however, the growth rate of each chain is adjusted based on the throughput of the other chain. There are three main benefits as follows:

_Stability_: if the Pow Chain becomes too difficulty to mine (as the result of too much mining power invested while less stake contributed to maintain the PoS chain), people are incentivized to work on the PoS chain (this chain is easier to mine because the PoS difficulty is the inverse of PoW difficulty). This helps with the issues of too much mining power spent for PoW blockchain as in Bitcoin for the same throughput of 10 minutes a block. Also, if the PoS chain is too difficulty to mine, then people are incentivezed to move to the PoW chain. We can see that the two leader elections (PoW and PoS) become a zero-sum game, and the system will itself adjust to a balance state.
_Throughput_: With PoS combined, the system can produce a block every 20 seconds like some other PoS systems.
_Security_: the adversary needs to have majority of both compuing power and stake to attack the system.

## 3.2 Blockchains Structure 
TBD

![](https://user-images.githubusercontent.com/82968967/116750675-d1052880-a9d0-11eb-99ce-2bcfb0058b53.png)
## 3.3 Zero-sum Blockchain PoW/PoS Difficulty Adjustment Mechanism
TBD



Hi, My name is Tuyet, I'm a blockchain research scientist/engineer. I'm also a wife and a mom of two beautiful princesses, Adalyn (was born in 2017 when I was in my second year PhD program) and Theodore (was born in Covid 2020).

![Screen Shot 2021-04-30 at 4 39 45 PM](https://user-images.githubusercontent.com/82968967/116751917-b633b380-a9d2-11eb-9fe8-c2e8b2826bdf.png)


Email: duongtt3@alumni.vcu.edu

