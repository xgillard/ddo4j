/**
 * This package contains the classes implementing solver frontiers.
 * 
 * In a first version of this library, and to keep things simple and easy, 
 * only one implementation will be provided: the simple frontier which 
 * is an almost direct mapping to a binary heap.
 * 
 * The NoDuplicateFrontier is implemented as a custom adaptative binary heap 
 * with uniqueness constraint. While it does offer the opportunity to solve many 
 * problems significantly faster than using the SimpleFrontier, one should refrain 
 * from always prefering the more advanced NoDuplicateFrontier as it imposes 
 * additional conditions on the model. Indeed for two 
 * this implementation to be usable, the model must guaranteed that subproblems
 * having one same root state are equivalent (might not always be the case).
 */
package be.uclouvain.ingi.aia.ddo4j.implem.frontier;
