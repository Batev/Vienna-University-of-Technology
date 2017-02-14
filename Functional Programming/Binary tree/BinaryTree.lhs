---------
Aufgabe 1
---------

Declarations: 

> data Tree a = Nil | Node a (Tree a) (Tree a) deriving (Eq,Ord,Show)

> data Order = Up | Down deriving (Eq,Show)

Support functions:

> insert' :: Ord a => a -> Tree a -> Tree a
> insert' n Nil 					= (Node n Nil Nil)
> insert' n (Node v Nil Nil)
>						| v == n 	= (Node v Nil Nil)
>						| v > n 	= insert' n (Node v (Node n Nil Nil) Nil)   
> 						| otherwise = insert' n (Node v Nil (Node n Nil Nil)) 
> insert' n (Node v l r)
>						| v > n     = Node v (insert' n l) r
>						| otherwise = Node v l (insert' n r)

> delete' :: (Ord a) => a -> Tree a -> Tree a
> delete' _ Nil = Nil
> delete' n (Node v l r)  
>		 				| n == v = delete'' (Node v l r)
>	 					| n < v  = Node v (delete' n l) r
>	 					| n > v  = Node v l (delete' n r)

> delete'' :: (Ord a) => Tree a -> Tree a 
> delete'' (Node v Nil r) = r
> delete'' (Node v l Nil) = l
> delete'' (Node v l r)   = (Node elem l (delete elem r)) where elem = (deleteMostLeft r)

> deleteMostLeft :: (Ord a) => Tree a -> a
> deleteMostLeft (Node v Nil _) = v
> deleteMostLeft (Node _ l _)   = deleteMostLeft l 

> flatten' :: Ord a => Order -> Tree a -> [a]
> flatten' _ Nil  			  = []
> flatten' Up (Node n l r)	  = flatten' Up l ++ [n] ++ flatten' Up r
> flatten' Down (Node n l r)  = flatten' Down r ++ [n] ++ flatten' Down l

Main functions:

> nil :: Tree a
> nil = Nil

> isNilTree :: Tree a -> Bool
> isNilTree Nil = True
> isNilTree t   = False

> isNodeTree :: Tree a -> Bool
> isNodeTree tree = not (isNilTree tree)

> leftSubTree :: Tree a -> Tree a
> leftSubTree Nil           = error "Empty Tree as Argument"
> leftSubTree (Node _ l _)  = l

> rightSubTree :: Tree a -> Tree a
> rightSubTree Nil           = error "Empty Tree as Argument"
> rightSubTree (Node _ _ r)  = r

> treeValue :: Tree a -> a
> treeValue Nil           = error "Empty Tree as Argument"
> treeValue (Node v _ _)  = v

> isValueOf :: Eq a => a -> Tree a -> Bool
> isValueOf t Nil 		   = False
> isValueOf t (Node v l r) = v == t || isValueOf t l || isValueOf t r

> isOrderedTree :: Ord a => Tree a -> Bool
> isOrderedTree Nil 		     = True
> isOrderedTree (Node v Nil Nil) = True
> isOrderedTree (Node v Nil r)   = v < (treeValue r) && isOrderedTree r
> isOrderedTree (Node v l Nil)   = v > (treeValue l) && isOrderedTree l
> isOrderedTree (Node v l r)     = (v > (treeValue l) && v < (treeValue r)) && isOrderedTree l && isOrderedTree r

> insert :: Ord a => a -> Tree a -> Tree a
> insert v tree 
>				| not (isOrderedTree tree) = error "Argument Tree not Ordered"
>				| (isValueOf v tree) 	   = tree
>				| (isNilTree tree)   	   = (Node v Nil Nil)
>				| otherwise 		 	   = insert' v tree

> delete :: Ord a => a -> Tree a -> Tree a
> delete v tree 
>				| not (isOrderedTree tree) = error "Argument Tree not Ordered"
>				| not (isValueOf v tree)   = tree
>				| (isNilTree tree)   	   = error "Cannot delete an element from an empty tree"
>				| otherwise 		 	   = delete' v tree

> flatten :: Ord a => Order -> Tree a -> [a]
> flatten order tree
>					| not (isOrderedTree tree) = error "Argument Tree not Ordered"
>					| otherwise 			   = flatten' order tree

---------
Aufgabe 2
---------

> maxLength :: Tree a -> Int
> maxLength Nil = 0
> maxLength (Node _ l r) = 1 + max (maxLength l) (maxLength r)

> minLength :: Tree a -> Int
> minLength Nil = 0
> minLength (Node _ l r) = 1 + min (minLength l) (minLength r)

> balancedDegree :: Tree a -> Int
> balancedDegree t = abs ((maxLength t) - (minLength t))