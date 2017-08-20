import random
import csv

from keras.models import Model

from keras.preprocessing.sequence import pad_sequences
from keras.preprocessing.text import Tokenizer

def read_csv(file_path):

    rows = []

    with open(file_path, "r") as csvfile:
        file = csvfile

        for line in file:
            line = line.replace("\xa0", " ")
            rows.append(line.split(","))

    return rows

def build_tokeniser(sentences):
    t = Tokenizer()
    t.fit_on_texts(sentences)
    return t

def get_category_indeces(category_names):
    category_dict = {}

    indexes = []

    highest_index = -1

    for name in category_names:

        if name not in category_dict:
            highest_index += 1
            category_dict[name] = highest_index

        indexes.append(category_dict[name])

    return indexes

def log_results(filename, model:Model, epochs, batch_size, x_val, y_val):
    with open(filename, "a") as logger:
        logger.write("Model summary:\n")
        logger.write(str(model.to_json()) + "\n")
        logger.write("Epochs trained: " + str(epochs) + "\n")
        logger.write("Batch size: " + str(batch_size) + "\n")
        score = model.evaluate(x_val, y_val, verbose=False)

        logger.write("Validation score:" + str(score) + "\n")

def test_input_sentence(tokenizer, model, max_sequence_length):
    sen = input("Enter test sentence: ")
    sequence = tokenizer.texts_to_sequences([sen])
    padded = pad_sequences(sequence, maxlen=max_sequence_length)
    print(model.predict(padded))
