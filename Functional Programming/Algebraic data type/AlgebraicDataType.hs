---------------
---Aufgabe 1---
---------------

-- Declarations: 

data Digit = Zero | One | Two
type Digits = [Digit]
data Sign = Pos | Neg

newtype Numeral = Num (Sign,Digits)

-- Support functions:

concatDigits :: Digits -> String
concatDigits []     = ""
concatDigits (x:xs) = (show x) ++ (concatDigits xs)

canonizeNum :: Numeral -> Numeral
canonizeNum (Num (sign, digits)) = Num (if ((head list) == Zero) && (length list) == 1 
                                      then Pos 
                                      else sign, 
                                      list)
                                      where list = (remove0s digits)

remove0s :: Digits -> Digits
remove0s digits
              | curr /= Zero || (length digits) == 1 = digits
              | otherwise                            = remove0s (drop 1 digits)
              where curr                             = (head digits)

canonize :: Digits -> Digits
canonize []        = error "Invalid Argument"
canonize [Zero]    = [Zero]
canonize (Zero:xs) = canonize xs
canonize list      = list

isNumeralValid :: Numeral -> Bool
isNumeralValid (Num (sign, digits))
                                  | null digits = False
                                  | otherwise   = True

toBase3 :: Integer -> Digits
toBase3 0 = [Zero]
toBase3 n = reverse (toBase3' n)

toBase3' :: Integer -> Digits
toBase3' 0 = []
toBase3' n
         | n `mod` 3 == 2 = Two  : toBase3' (n `div` 3)
         | n `mod` 3 == 1 = One  : toBase3' (n `div` 3)
         | n `mod` 3 == 0 = Zero : toBase3' (n `div` 3)

fromBase3 :: Digits -> Int
fromBase3 digits = fromBase3' (reverse digits) 0 0

fromBase3' :: Digits -> Int -> Int -> Int
fromBase3' digits pow ans
                          | (null digits)        = ans
                          | curr == Zero         =  fromBase3' (drop 1 digits) (pow+1) (ans + (0*(3^pow)))
                          | curr == One          =  fromBase3' (drop 1 digits) (pow+1) (ans + (1*(3^pow)))
                          | otherwise            =  fromBase3' (drop 1 digits) (pow+1) (ans + (2*(3^pow)))
                          where curr             = (head digits)

int2num :: Integer -> Numeral
int2num n = Num (sign, digits) where
              sign   = (if n >= 0 then Pos else Neg)
              digits = (toBase3 (abs n))

num2int :: Numeral -> Integer
num2int (Num (sign, digits))
                              | isNumeralValid (Num (sign, digits)) = if (sign == Pos) || (((head digits) == Zero) && ((length digits) == 1)) then number else (number * (-1))
                              | otherwise                           = error "Invalid Argument"
                               where number = (toInteger (fromBase3 (canonize digits)))

insertAt :: a -> [a] -> Int -> [a]
insertAt x ys     1 = x:ys
insertAt x (y:ys) n = y:insertAt x ys (n-1)

deleteAt :: Int -> [a] -> [a]
deleteAt 0 (x:xs) = xs
deleteAt n (x:xs) | n >= 0 = x : (deleteAt (n-1) xs)
deleteAt _ _ = error "index out of range"

inc' :: Numeral -> Int -> Numeral
inc' (Num (sign, digits)) n
                      | length digits == n && n /= 0               = Num(sign, (insertAt One digits 1)) 
                      | curr == Zero                               = Num(sign, (insertAt One (deleteAt (pos) digits) (pos+1)))
                      | curr == One                                = Num(sign, (insertAt Two (deleteAt (pos) digits) (pos+1)))
                      | otherwise                                  = inc' (Num(sign, (insertAt Zero (deleteAt (pos) digits) (pos+1)))) (n+1)
                      where 
                      curr                           = (digits !! (pos))
                      pos                            = ((length digits) - n - 1)

dec' :: Numeral -> Int -> Numeral
dec' (Num (sign, digits)) n
                      | length digits == n && n /= 0 = Num(Neg, ([One])) 
                      | curr == Two                  = Num(sign, (insertAt One (deleteAt (pos) digits) (pos+1)))
                      | curr == One                  = Num(sign, (insertAt Zero (deleteAt (pos) digits) (pos+1)))
                      | otherwise                    = dec' (Num(sign, (insertAt Two (deleteAt (pos) digits) (pos+1)))) (n+1)
                      where 
                      curr                           = (digits !! (pos))
                      pos                            = ((length digits) - n - 1)

inc :: Numeral -> Numeral
inc (Num (sign, digits))
                          | (canonizeNum (Num (sign, digits))) == (Num (Neg, [One])) = (Num (Pos, [Zero]))
                          | isNumeralValid (Num (sign, digits))                   = if (sign == Pos) then (inc' (canonizeNum (Num (sign, digits))) 0) else (dec' (canonizeNum (Num (sign, digits))) 0)
                          | otherwise                                             = error "Invalid Argument"

dec :: Numeral -> Numeral
dec (Num (sign, digits))
                          | isNumeralValid (Num (sign, digits)) = if (sign == Pos) then canonizeNum (dec' (canonizeNum (Num (sign, digits))) 0) else canonizeNum (inc' (canonizeNum (Num (sign, digits))) 0)
                          | otherwise                           = error "Invalid Argument"

numAdd' :: Numeral -> Numeral -> Numeral
numAdd' (Num (sign1, digits1)) (Num (sign2, digits2)) 
                                                  | (Num (sign1, digits1)) == (Num (Pos, [Zero])) = (Num (sign2, digits2))
                                                  | (Num (sign2, digits2)) == (Num (Pos, [Zero])) = (Num (sign1, digits1))
                                                  | otherwise                                     = numAdd' (dec (Num (sign1, digits1))) (inc (Num (sign2, digits2)))

numAdd :: Numeral -> Numeral -> Numeral
numAdd (Num (sign1, digits1)) (Num (sign2, digits2))
                                                  | sign1 == Pos && sign2 == Pos = (numAdd' (Num (sign1, digits1)) (Num (sign2, digits2)))
                                                  | sign2 == Pos && sign2 == Neg = (numAdd' (Num (sign1, digits1)) (Num (sign2, digits2)))
                                                  | sign1 == Neg && sign2 == Pos = (numAdd' (Num (sign2, digits2)) (Num (sign1, digits1)))
                                                  | otherwise                    = (numAdd' (Num (sign1, digits1)) (Num (sign2, digits2)))                               

-- Main functions:

--- Digit ---

-- a) Eq
instance Eq Digit where
	(==) Zero Zero = True
	(==) One One   = True
	(==) Two Two   = True 
	(==) _ _       = False

-- b) Show
instance Show Digit where
    show Zero = "0"
    show One  = "1"
    show Two  = "2"

-- c) Ord
instance Ord Digit where
    compare d1 d2
                | d1 == d2   = EQ
                | d1 == Zero = LT
                | d2 == Zero = GT
                | d1 == One  = LT
                | d2 == One  = GT
                | d2 == Two  = LT
                | otherwise  = GT

--- \ ---

--- Sign ---

-- a) Eq
instance Eq Sign where
	(==) Pos Pos = True 
	(==) Neg Neg = True
	(==) _ _     = False

-- b) Show
instance Show Sign where
    show Pos = "+"
    show Neg = "-"

--- \ ---

--- Numeral ---

-- a) Eq
instance Eq Numeral where
    (==) (Num (sign1,digits1)) (Num (sign2,digits2)) = (sign1 == sign2) && ((canonize digits1) == (canonize digits2))

-- b) Show
instance Show Numeral where
    show (Num (_,[Zero]))     = "+0"
    show (Num (sign, digits)) = (show sign) ++ (concatDigits (canonize digits))

-- c) Ord
instance Ord Numeral where
    compare (Num(Pos,_)) (Num(Neg,_))             = GT
    compare (Num(Neg,_)) (Num(Pos,_))             = LT
    compare (Num(Pos,digits1)) (Num(Pos,digits2)) = (compare (canonize digits1) (canonize digits2))
    compare (Num(Neg,digits1)) (Num(Neg,digits2))
                                        | r == EQ = EQ
                                        | r == GT = LT
                                        | r == LT = GT
                                          where r = (compare (canonize digits1) (canonize digits2))

-- d) Num
instance Num Numeral where
    negate (Num (Pos,digits)) = (Num (Neg,digits))
    negate (Num (Neg,digits)) = (Num (Pos,digits))
    (-) a b = numAdd a (negate b)
    (+) a b = numAdd a b
    abs (Num (sign, digits)) = (Num (Pos, (canonize digits)))
    (*) a b = (int2num ((num2int a) * (num2int b)))
    fromInteger a = (int2num a)
    signum (Num (sign, digits))
                            | (null digits)               = error "Invalid Argument"
                            | (canonize digits) == [Zero] = 0
                            | sign == Pos                 = 1
                            | otherwise                   = (-1)
--- \ ---