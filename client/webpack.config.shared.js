'use strict';

var webpack = require('webpack');

const ExtractTextPlugin = require("extract-text-webpack-plugin");

module.exports = {
    plugins: [
        new webpack.NoEmitOnErrorsPlugin(),
        new ExtractTextPlugin('style.css')
    ],
    module: {
        rules: [
            {
              // Transform our own .css files with PostCSS and CSS-modules
              test: /\.css$/,
              exclude: /node_modules/,
              use: ['style-loader', 'css-loader'],
            }, {
              // Do not transform vendor's CSS with CSS-modules
              // The point is that they remain in global scope.
              // Since we require these CSS files in our JS or CSS files,
              // they will be a part of our compilation either way.
              // So, no need for ExtractTextPlugin here.
              test: /\.css$/,
              include: /node_modules/,
              use: ['style-loader', 'css-loader'],
            }, {
                test: /\.(png|jpg|gif|svg|eot|ttf|woff|woff2)$/,
                loader: 'url-loader',
                options: {
                    limit: 20000
                }
            }]
    }
};
