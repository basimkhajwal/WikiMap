import random

from keras.preprocessing.text import text_to_word_sequence
from keras.preprocessing.text import one_hot

from util import *

sentences = [row[1] for row in read_csv("C:\\Users\\Jamie\\Documents\\Documents\\WikiCategoryData.csv")][1:]

print(sentences)